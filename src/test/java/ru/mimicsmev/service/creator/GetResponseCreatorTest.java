package ru.mimicsmev.service.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mimicsmev.config.MimicProperties;
import ru.mimicsmev.dao.content.TestContentRequest;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqGetResponse;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetResponseRepository;
import ru.mimicsmev.dao.repository.ReqLogResponseRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.AppException;
import v1.GetResponseResponse;
import v1.MessageMetadata;
import v1.MessageTypeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static ru.mimicsmev.testutils.Utils.getObjectMapper;
import static ru.mimicsmev.testutils.Utils.marshalToString;
import static ru.mimicsmev.testutils.Utils.nodeToString;
import static ru.mimicsmev.testutils.Utils.unmarshal;

@ExtendWith(MockitoExtension.class)
class GetResponseCreatorTest {
    @Mock
    private ReqGetResponseRepository reqGetResponseRepository;
    @Mock
    private VsListRepository vsListRepository;
    @Mock
    private ReqLogResponseRepository reqLogResponseRepository;
    @Mock
    private ReqAttachmentRepository reqAttachmentRepository;
    private final ObjectMapper objectMapper = getObjectMapper();

    @Test
    @DisplayName("Build GetResponseResponse")
    void buildResponse() throws JAXBException, AppException, IOException {
        MimicProperties mimicProperties = new MimicProperties();
        String receiptMnemonic = "my-mnemonic";
        String receiptMnemonicDesc = "my-mnemonic-desc";
        mimicProperties.getInternalVs().setMnemonic(receiptMnemonic);
        mimicProperties.getInternalVs().setMnemonicDesc(receiptMnemonicDesc);
        GetResponseCreator getResponseCreator = new GetResponseCreator(null, reqGetResponseRepository, vsListRepository, reqLogResponseRepository, reqAttachmentRepository, mimicProperties, objectMapper);

        String rootTag = "TestTag";
        String senderMnemonic = "mnemonic-vs";
        String senderMnemonicDesc = "mnemonic-vs-desc";
        VsList vsList = new VsList();
        vsList.setRootTag(rootTag);
        vsList.setMnemonic(senderMnemonic);
        vsList.setMnemonicDesc(senderMnemonicDesc);

        when(vsListRepository.findVsListByRootTag(rootTag)).thenReturn(vsList);

        ReqGetResponse reqGetResponse = new ReqGetResponse();
        reqGetResponse.setReqStatus(ReqStatus.NEW);
        reqGetResponse.setId(1L);
        String msgId = UUID.randomUUID().toString();
        String originalMsgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");
        reqGetResponse.setRootTag(rootTag);
        reqGetResponse.setMsgId(msgId);
        reqGetResponse.setContent(marshalToString(testContentRequest));
        reqGetResponse.setOriginalMsgId(originalMsgId);
        List<ReqAttachment> attachmentList = new ArrayList<>();

        ReqAttachment reqAttachment = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123".getBytes());
        reqAttachment.setAttachRow("123123123");
        reqAttachment.setAttachName("file-nam");
        attachmentList.add(reqAttachment);
        when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.GetResponse)).thenReturn(attachmentList);

        GetResponseResponse responseResponse = getResponseCreator.buildResponse(reqGetResponse);

        assertEquals(msgId, responseResponse.getResponseMessage().getResponse().getSenderProvidedResponseData().getMessageID());
        assertEquals(originalMsgId, responseResponse.getResponseMessage().getResponse().getOriginalMessageId());
        assertEquals(testContentRequest, unmarshal(nodeToString(responseResponse.getResponseMessage().getResponse().getSenderProvidedResponseData().getMessagePrimaryContent().getAny())));

        assertNotNull(responseResponse.getResponseMessage().getAttachmentContentList());
        assertEquals(1, responseResponse.getResponseMessage().getAttachmentContentList().getAttachmentContent().size());

        assertEquals("123123123", new String(responseResponse.getResponseMessage().getAttachmentContentList().getAttachmentContent().get(0).getContent().getInputStream().readAllBytes()));
        assertEquals("application/zip", responseResponse.getResponseMessage().getAttachmentContentList().getAttachmentContent().get(0).getContent().getContentType());
        assertEquals("file-nam.zip", responseResponse.getResponseMessage().getAttachmentContentList().getAttachmentContent().get(0).getId());
        MessageMetadata messageMetadata = responseResponse.getResponseMessage().getResponse().getMessageMetadata();

        assertAll(
                "Group assert Metadata",
                () -> assertEquals(msgId, messageMetadata.getMessageId()),
                () -> assertEquals(senderMnemonic, messageMetadata.getRecipient().getMnemonic()),
                () -> assertEquals(senderMnemonicDesc, messageMetadata.getRecipient().getHumanReadableName()),
                () -> assertEquals(receiptMnemonic, messageMetadata.getSender().getMnemonic()),
                () -> assertEquals(receiptMnemonicDesc, messageMetadata.getSender().getHumanReadableName()),
                () -> assertEquals(MessageTypeType.REQUEST, messageMetadata.getMessageType()),
                () -> assertNotNull(messageMetadata.getSendingTimestamp()),
                () -> assertNotNull(messageMetadata.getDeliveryTimestamp())
        );

        ReqAttachment reqAttachmentTwo = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123Two".getBytes());
        reqAttachment.setAttachRow("123123123Two");
        reqAttachment.setAttachName("file-nam-am");
        attachmentList.add(reqAttachmentTwo);
        when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.GetResponse)).thenReturn(attachmentList);
        responseResponse = getResponseCreator.buildResponse(reqGetResponse);
        assertNotNull(responseResponse.getResponseMessage().getAttachmentContentList());
        assertEquals(2, responseResponse.getResponseMessage().getAttachmentContentList().getAttachmentContent().size());


    }
}