package ru.mimicsmev.service.creator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mimicsmev.config.MimicProperties;
import ru.mimicsmev.dao.entity.ReqSendRequest;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqSendRequestRepository;
import ru.mimicsmev.exception.SmevInvalidContentException;
import ru.mimicsmev.service.AbstractSmevResponseCreator;
import v1.SendRequestRequest;
import v1.SendRequestResponse;

import java.util.UUID;

@Component
@Slf4j
public class SendRequestCreator extends AbstractSmevResponseCreator<SendRequestResponse, SendRequestRequest> {

    private final ReqSendRequestRepository sendRequestRepository;
    private final ReqAttachmentRepository reqAttachmentRepository;
    private final MimicProperties mimicProperties;

    protected SendRequestCreator(Jaxb2Marshaller jaxb2Marshaller, ReqSendRequestRepository sendRequestRepository, ReqAttachmentRepository reqAttachmentRepository, MimicProperties mimicProperties) {
        super(jaxb2Marshaller);
        this.sendRequestRepository = sendRequestRepository;
        this.reqAttachmentRepository = reqAttachmentRepository;
        this.mimicProperties = mimicProperties;
    }

    @Override
    @Transactional(noRollbackFor = {SmevInvalidContentException.class})
    public SendRequestResponse createResponse(SendRequestRequest request) throws SmevInvalidContentException {
        try {
            ReqSendRequest reqSendRequest = new ReqSendRequest();
            reqSendRequest.setReqRow(marshalToString(request));
            reqSendRequest.setContent(nodeToString(request.getSenderProvidedRequestData().getMessagePrimaryContent().getAny()));
            reqSendRequest.setMsgId(request.getSenderProvidedRequestData().getMessageID());
            reqSendRequest.setRootTag(request.getSenderProvidedRequestData().getMessagePrimaryContent().getAny().getLocalName());
            reqSendRequest.setReqStatus(ReqStatus.RECEIVED);
            sendRequestRepository.save(reqSendRequest);
            if (request.getAttachmentContentList() != null &&
                    !request.getAttachmentContentList().getAttachmentContent().isEmpty()) {
                reqAttachmentRepository.saveAll(map(request.getAttachmentContentList().getAttachmentContent(), reqSendRequest.getId(), ReqType.SendRequest));
            }
            SendRequestResponse sendRequestResponse = new SendRequestResponse();
            sendRequestResponse.setMessageMetadata(createMetadata(mimicProperties.getInternalVs(), null, UUID.randomUUID().toString()));
            sendRequestResponse.setSMEVSignature(sigXmlContent(sendRequestResponse.getMessageMetadata()));
            return sendRequestResponse;
        } catch (Exception e) {
            log.error("Failed to accept response", e);
            throw new SmevInvalidContentException(String.format("Failed to accept response: %s", e.getMessage()));
        }
    }
}
