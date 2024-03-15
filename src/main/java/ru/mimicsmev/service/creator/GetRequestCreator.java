package ru.mimicsmev.service.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.mimicsmev.config.MimicProperties;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqGetRequest;
import ru.mimicsmev.dao.entity.ReqLogRequest;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetRequestRepository;
import ru.mimicsmev.dao.repository.ReqLogRequestRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.SmevInvalidContentException;
import ru.mimicsmev.service.AbstractSmevResponseCreator;
import v1.AttachmentContentList;
import v1.AttachmentContentType;
import v1.AttachmentHeaderList;
import v1.AttachmentHeaderType;
import v1.GetRequestRequest;
import v1.GetRequestResponse;
import v1.MessagePrimaryContent;
import v1.MessageTypeSelector;
import v1.Request;
import v1.SenderProvidedRequestData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static ru.mimicsmev.service.SignatureService.sigAttach;

@Component
@Slf4j
public class GetRequestCreator extends AbstractSmevResponseCreator<GetRequestResponse, GetRequestRequest> {
    private final VsListRepository vsListRepository;
    private final MimicProperties mimicProperties;
    private final ObjectMapper objectMapper;
    private final ReqGetRequestRepository reqGetRequestRepository;
    private final ReqLogRequestRepository reqLogRequestRepository;
    private final ReqAttachmentRepository reqAttachmentRepository;

    protected GetRequestCreator(Jaxb2Marshaller jaxb2Marshaller, VsListRepository vsListRepository, MimicProperties mimicProperties, ObjectMapper objectMapper, ReqGetRequestRepository reqGetRequestRepository, ReqLogRequestRepository reqLogRequestRepository, ReqAttachmentRepository reqAttachmentRepository) {
        super(jaxb2Marshaller);
        this.vsListRepository = vsListRepository;
        this.mimicProperties = mimicProperties;
        this.objectMapper = objectMapper;
        this.reqGetRequestRepository = reqGetRequestRepository;
        this.reqLogRequestRepository = reqLogRequestRepository;
        this.reqAttachmentRepository = reqAttachmentRepository;
    }

    @Override
    @Transactional(noRollbackFor = {SmevInvalidContentException.class})
    public GetRequestResponse createResponse(GetRequestRequest request) throws SmevInvalidContentException {
        try {
            ReqLogRequest logRequest = new ReqLogRequest();
            logRequest.setReqRow(marshalToString(request));
            reqLogRequestRepository.save(logRequest);
            Optional<MessageTypeSelector> messageTypeSelectorOptional = Optional.of(request.getMessageTypeSelector());
            MessageTypeSelector messageTypeSelector = messageTypeSelectorOptional.orElseThrow(() -> new SmevInvalidContentException("Unable extract MessageTypeSelector"));
            ReqGetRequest reqGetRequest = StringUtils.hasLength(messageTypeSelector.getRootElementLocalName()) ? reqGetRequestRepository.getOneByStatusAndRootTag(ReqStatus.NEW.name(), messageTypeSelector.getRootElementLocalName()) : reqGetRequestRepository.getOneByStatus(ReqStatus.NEW.name());
            if (reqGetRequest == null) {
                return new GetRequestResponse();
            }
            GetRequestResponse response = buildRequest(reqGetRequest);
            reqGetRequest.setRefId(logRequest.getId());
            reqGetRequest.setReqStatus(ReqStatus.SENT);
            reqGetRequestRepository.save(reqGetRequest);
            return response;

        } catch (Exception e) {
            log.error("Failed to accept request", e);
            throw new SmevInvalidContentException(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public GetRequestResponse buildRequest(@NonNull ReqGetRequest reqGetRequest) {
        try {
            VsList mnemonic = vsListRepository.findVsListByRootTag(reqGetRequest.getRootTag());

            GetRequestResponse getRequestResponse = new GetRequestResponse();
            GetRequestResponse.RequestMessage requestMessage = new GetRequestResponse.RequestMessage();

            Request request = new Request();
            request.setId("SIGNED_BY_SMEV");

            SenderProvidedRequestData senderProvidedRequestData = new SenderProvidedRequestData();
            senderProvidedRequestData.setId("SIGNED_BY_CALLER");
            senderProvidedRequestData.setMessageID(reqGetRequest.getMsgId());

            MessagePrimaryContent messagePrimaryContent = new MessagePrimaryContent();
            messagePrimaryContent.setAny(createElementContent(reqGetRequest.getContent()));
            senderProvidedRequestData.setMessagePrimaryContent(messagePrimaryContent);
            List<ReqAttachment> reqAttachmentList = reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(reqGetRequest.getId(), ReqType.GetRequest);
            if(!reqAttachmentList.isEmpty()) {
                AttachmentContentList attachmentContentList = new AttachmentContentList();
                AttachmentHeaderList attachmentHeaderList = new AttachmentHeaderList();
                for (ReqAttachment reqAttachment : reqAttachmentList) {
                    AttachmentContentType attachmentContentType = map(reqAttachment);
                    attachmentContentList.getAttachmentContent().add(attachmentContentType);

                    AttachmentHeaderType attachmentHeaderType = new AttachmentHeaderType();
                    attachmentHeaderType.setMimeType("application/zip");
                    attachmentHeaderType.setContentId(reqAttachment.getAttachName() + ".zip");
                    attachmentHeaderType.setSignaturePKCS7(sigAttach(reqAttachment.getAttachBlob()));
                    attachmentHeaderList.getAttachmentHeader().add(attachmentHeaderType);
                }
                senderProvidedRequestData.setAttachmentHeaderList(attachmentHeaderList);
                requestMessage.setAttachmentContentList(attachmentContentList);
            }

            request.setMessageMetadata(createMetadata(mnemonic, mimicProperties.getInternalVs(), reqGetRequest.getMsgId()));
            request.setSenderProvidedRequestData(senderProvidedRequestData);
            String replyTo = new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(ReplyTo.builder().mid(reqGetRequest.getMsgId()).mnm(mnemonic.getMnemonic()).build()).getBytes(StandardCharsets.UTF_8)));
            request.setReplyTo(replyTo);
            request.setSenderInformationSystemSignature(sigXmlContent(senderProvidedRequestData));
            requestMessage.setRequest(request);
            requestMessage.setSMEVSignature(sigXmlContent(request));
            getRequestResponse.setRequestMessage(requestMessage);
            return getRequestResponse;

        } catch (Exception e) {
            log.error("Unable build request message", e);
            reqGetRequest.setReqStatus(ReqStatus.ERROR_BUILD);
            reqGetRequestRepository.save(reqGetRequest);
            throw new SmevInvalidContentException("Unable build request message: %s".formatted(e.getMessage()), e);
        }
    }
}
