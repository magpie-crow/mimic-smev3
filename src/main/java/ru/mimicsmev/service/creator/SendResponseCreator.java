package ru.mimicsmev.service.creator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mimicsmev.config.MimicProperties;
import ru.mimicsmev.dao.entity.ReqSendResponse;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqSendResponseRepository;
import ru.mimicsmev.exception.SmevInvalidContentException;
import ru.mimicsmev.service.AbstractSmevResponseCreator;
import v1.MessageMetadata;
import v1.SendResponseRequest;
import v1.SendResponseResponse;

import java.util.UUID;

@Component
@Slf4j
public class SendResponseCreator extends AbstractSmevResponseCreator<SendResponseResponse, SendResponseRequest> {
    private final MimicProperties mimicProperties;
    private final ReqSendResponseRepository sendResponseRepository;
    private final ReqAttachmentRepository reqAttachmentRepository;

    SendResponseCreator(Jaxb2Marshaller jaxb2Marshaller, MimicProperties mimicProperties, ReqSendResponseRepository sendResponseRepository, ReqAttachmentRepository reqAttachmentRepository) {
        super(jaxb2Marshaller);
        this.mimicProperties = mimicProperties;
        this.sendResponseRepository = sendResponseRepository;
        this.reqAttachmentRepository = reqAttachmentRepository;
    }

    private ReqSendResponse createReqSendResponse(String s) {
        ReqSendResponse sendResponse = new ReqSendResponse();
        sendResponse.setReqRow(s);
        sendResponseRepository.save(sendResponse);
        return sendResponse;
    }

    @Override
    @Transactional(noRollbackFor = {SmevInvalidContentException.class})
    public SendResponseResponse createResponse(SendResponseRequest request) throws SmevInvalidContentException {
        try {
            ReqSendResponse sendResponse = sendResponseRepository.save(createReqSendResponse(marshalToString(request)));
            if (request.getAttachmentContentList() != null &&
                    !request.getAttachmentContentList().getAttachmentContent().isEmpty()) {
                reqAttachmentRepository.saveAll(map(request.getAttachmentContentList().getAttachmentContent(), sendResponse.getId(), ReqType.SendResponse));
            }
            SendResponseResponse sendResponseResponse = new SendResponseResponse();
            MessageMetadata metadata = createMetadata(mimicProperties.getInternalVs(), null, UUID.randomUUID().toString());
            sendResponseResponse.setMessageMetadata(metadata);
            sendResponseResponse.setSMEVSignature(sigXmlContent(metadata));
            return sendResponseResponse;

        } catch (Exception e) {
            log.error("Unable create SendResponseResponse", e);
            throw new SmevInvalidContentException(String.format("Unable create SendResponseResponse : %s", e.getMessage()), e);
        }
    }
}
