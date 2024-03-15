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
import ru.mimicsmev.dao.entity.ReqGetRequest;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetRequestRepository;
import ru.mimicsmev.dao.repository.ReqLogRequestRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.AppException;
import v1.GetRequestResponse;
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
class GetRequestCreatorTest {
    @Mock
    private VsListRepository vsListRepository;
    private final ObjectMapper objectMapper = getObjectMapper();
    @Mock
    private ReqGetRequestRepository reqGetRequestRepository;
    @Mock
    private ReqLogRequestRepository reqLogRequestRepository;
    @Mock
    private ReqAttachmentRepository reqAttachmentRepository;

    @Test
    @DisplayName("Build GetRequestResponse")
    void buildRequest() throws JAXBException, IOException, AppException {
        MimicProperties mimicProperties = new MimicProperties();
        String receiptMnemonic = "my-mnemonic";
        String receiptMnemonicDesc = "my-mnemonic-desc";
        mimicProperties.getInternalVs().setMnemonic(receiptMnemonic);
        mimicProperties.getInternalVs().setMnemonicDesc(receiptMnemonicDesc);
        GetRequestCreator getRequestCreator = new GetRequestCreator(null, vsListRepository, mimicProperties, objectMapper, reqGetRequestRepository, reqLogRequestRepository, reqAttachmentRepository);
        String rootTag = "TestTag";
        String senderMnemonic = "mnemonic-vs";
        String senderMnemonicDesc = "mnemonic-vs-desc";
        VsList vsList = new VsList();
        vsList.setRootTag(rootTag);
        vsList.setMnemonic(senderMnemonic);
        vsList.setMnemonicDesc(senderMnemonicDesc);

        when(vsListRepository.findVsListByRootTag(rootTag)).thenReturn(vsList);
        ReqGetRequest getRequest = new ReqGetRequest();
        getRequest.setReqStatus(ReqStatus.NEW);
        getRequest.setId(1L);
        String msgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");
        getRequest.setRootTag(rootTag);
        getRequest.setMsgId(msgId);
        getRequest.setContent(marshalToString(testContentRequest));
        List<ReqAttachment> attachmentList = new ArrayList<>();

        ReqAttachment reqAttachment = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123".getBytes());
        reqAttachment.setAttachRow("123123123");
        reqAttachment.setAttachName("file-nam");
        attachmentList.add(reqAttachment);
        when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.GetRequest)).thenReturn(attachmentList);

        GetRequestResponse getRequestResponse = getRequestCreator.buildRequest(getRequest);

        assertEquals(msgId, getRequestResponse.getRequestMessage().getRequest().getSenderProvidedRequestData().getMessageID());
        assertEquals(testContentRequest, unmarshal(nodeToString(getRequestResponse.getRequestMessage().getRequest().getSenderProvidedRequestData().getMessagePrimaryContent().getAny())));

        assertNotNull(getRequestResponse.getRequestMessage().getAttachmentContentList());
        assertEquals(1, getRequestResponse.getRequestMessage().getAttachmentContentList().getAttachmentContent().size());

        assertEquals("123123123", new String(getRequestResponse.getRequestMessage().getAttachmentContentList().getAttachmentContent().get(0).getContent().getInputStream().readAllBytes()));
        assertEquals("application/zip", getRequestResponse.getRequestMessage().getAttachmentContentList().getAttachmentContent().get(0).getContent().getContentType());
        assertEquals("file-nam.zip", getRequestResponse.getRequestMessage().getAttachmentContentList().getAttachmentContent().get(0).getId());

        MessageMetadata messageMetadata = getRequestResponse.getRequestMessage().getRequest().getMessageMetadata();
        assertAll(
                "Group assert Metadata",
                () -> assertEquals(msgId, messageMetadata.getMessageId()),
                () -> assertEquals(senderMnemonic, messageMetadata.getSender().getMnemonic()),
                () -> assertEquals(senderMnemonicDesc, messageMetadata.getSender().getHumanReadableName()),
                () -> assertEquals(receiptMnemonic, messageMetadata.getRecipient().getMnemonic()),
                () -> assertEquals(receiptMnemonicDesc, messageMetadata.getRecipient().getHumanReadableName()),
                () -> assertEquals(MessageTypeType.REQUEST, messageMetadata.getMessageType()),
                () -> assertNotNull(messageMetadata.getSendingTimestamp()),
                () -> assertNotNull(messageMetadata.getDeliveryTimestamp())
        );

        ReqAttachment reqAttachmentTwo = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123Two".getBytes());
        reqAttachment.setAttachRow("123123123Two");
        reqAttachment.setAttachName("file-nam-am");
        attachmentList.add(reqAttachmentTwo);
        when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.GetRequest)).thenReturn(attachmentList);
        getRequestResponse = getRequestCreator.buildRequest(getRequest);
        assertNotNull(getRequestResponse.getRequestMessage().getAttachmentContentList());
        assertEquals(2, getRequestResponse.getRequestMessage().getAttachmentContentList().getAttachmentContent().size());
    }


}