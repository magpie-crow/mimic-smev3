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
import ru.mimicsmev.dao.entity.ReqGetResponse;
import ru.mimicsmev.dao.entity.ReqLogResponse;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetResponseRepository;
import ru.mimicsmev.dao.repository.ReqLogResponseRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.SmevInvalidContentException;
import ru.mimicsmev.service.AbstractSmevResponseCreator;
import v1.AttachmentContentList;
import v1.AttachmentContentType;
import v1.AttachmentHeaderList;
import v1.AttachmentHeaderType;
import v1.GetResponseRequest;
import v1.GetResponseResponse;
import v1.MessagePrimaryContent;
import v1.MessageTypeSelector;
import v1.Response;
import v1.SenderProvidedResponseData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static ru.mimicsmev.service.SignatureService.sigAttach;

@Component
@Slf4j
public class GetResponseCreator extends AbstractSmevResponseCreator<GetResponseResponse, GetResponseRequest> {
    private final ReqGetResponseRepository reqGetResponseRepository;
    private final VsListRepository vsListRepository;
    private final ReqLogResponseRepository reqLogResponseRepository;
    private final ReqAttachmentRepository reqAttachmentRepository;
    private final MimicProperties mimicProperties;
    private final ObjectMapper objectMapper;

    protected GetResponseCreator(Jaxb2Marshaller jaxb2Marshaller,
                                 ReqGetResponseRepository reqGetResponseRepository,
                                 VsListRepository vsListRepository,
                                 ReqLogResponseRepository reqLogResponseRepository, ReqAttachmentRepository reqAttachmentRepository, MimicProperties mimicProperties, ObjectMapper objectMapper) {
        super(jaxb2Marshaller);
        this.reqGetResponseRepository = reqGetResponseRepository;
        this.vsListRepository = vsListRepository;
        this.reqLogResponseRepository = reqLogResponseRepository;
        this.reqAttachmentRepository = reqAttachmentRepository;
        this.mimicProperties = mimicProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(noRollbackFor = {SmevInvalidContentException.class})
    public GetResponseResponse createResponse(GetResponseRequest request) throws SmevInvalidContentException {
        try {
            ReqLogResponse logResponse = new ReqLogResponse();
            logResponse.setReqRow(marshalToString(request));
            reqLogResponseRepository.save(logResponse);

            Optional<MessageTypeSelector> messageTypeSelectorOptional = Optional.of(request.getMessageTypeSelector());
            MessageTypeSelector messageTypeSelector = messageTypeSelectorOptional.orElseThrow(() -> new SmevInvalidContentException("Unable extract MessageTypeSelector"));
            ReqGetResponse response = StringUtils.hasLength(messageTypeSelector.getRootElementLocalName()) ? reqGetResponseRepository.getOneByStatusAndRootTag(ReqStatus.NEW.name(), messageTypeSelector.getRootElementLocalName()) : reqGetResponseRepository.getOneByStatus(ReqStatus.NEW.name());
            if (response == null) {
                return new GetResponseResponse();
            }
            GetResponseResponse doReturn = buildResponse(response);
            response.setRefId(logResponse.getId());
            response.setReqStatus(ReqStatus.SENT);
            reqGetResponseRepository.save(response);
            return doReturn;

        } catch (Exception e) {
            log.error("Failed to accept response", e);
            throw new SmevInvalidContentException(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public GetResponseResponse buildResponse(@NonNull ReqGetResponse reqGetResponse) {
        try {
            VsList mnemonic = vsListRepository.findVsListByRootTag(reqGetResponse.getRootTag());
            GetResponseResponse responseResponse = new GetResponseResponse();
            GetResponseResponse.ResponseMessage responseMessage = new GetResponseResponse.ResponseMessage();
            Response response = new Response();
            response.setId("SIGNED_BY_SMEV");
            response.setOriginalMessageId(reqGetResponse.getOriginalMsgId());

            SenderProvidedResponseData senderProvidedResponseData = new SenderProvidedResponseData();
            senderProvidedResponseData.setId("SIGNED_BY_CALLER");
            senderProvidedResponseData.setMessageID(reqGetResponse.getMsgId());
            String replyTo = new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(ReplyTo.builder().mid(reqGetResponse.getMsgId()).mnm(mnemonic.getMnemonic()).build()).getBytes(StandardCharsets.UTF_8)));
            senderProvidedResponseData.setTo(replyTo);

            MessagePrimaryContent messagePrimaryContent = new MessagePrimaryContent();
            messagePrimaryContent.setAny(createElementContent(reqGetResponse.getContent()));

            senderProvidedResponseData.setMessagePrimaryContent(messagePrimaryContent);


            List<ReqAttachment> reqAttachmentList = reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(reqGetResponse.getId(), ReqType.GetResponse);
            if (!reqAttachmentList.isEmpty()) {
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
                senderProvidedResponseData.setAttachmentHeaderList(attachmentHeaderList);
                responseMessage.setAttachmentContentList(attachmentContentList);
            }
            response.setSenderProvidedResponseData(senderProvidedResponseData);
            response.setMessageMetadata(createMetadata(mimicProperties.getInternalVs(), mnemonic, reqGetResponse.getMsgId()));
            response.setSenderInformationSystemSignature(sigXmlContent(messagePrimaryContent));
            responseMessage.setResponse(response);
            responseMessage.setSMEVSignature(sigXmlContent(response));
            responseResponse.setResponseMessage(responseMessage);
            return responseResponse;

        } catch (Exception e) {
            log.error("Unable build response message", e);
            reqGetResponse.setReqStatus(ReqStatus.ERROR_BUILD);
            reqGetResponseRepository.save(reqGetResponse);
            throw new SmevInvalidContentException("Unable build response message: %s".formatted(e.getMessage()), e);
        }

    }

}
