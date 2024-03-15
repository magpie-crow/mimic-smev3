package ru.mimicsmev.service.creator;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;
import ru.mimicsmev.config.MimicProperties;
import ru.mimicsmev.dao.content.TestContentRequest;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqSendRequest;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqSendRequestRepository;
import ru.mimicsmev.exception.SmevInvalidContentException;
import v1.AttachmentContentList;
import v1.AttachmentContentType;
import v1.AttachmentHeaderList;
import v1.AttachmentHeaderType;
import v1.MessagePrimaryContent;
import v1.SendRequestRequest;
import v1.SendRequestResponse;
import v1.SenderProvidedRequestData;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static ru.mimicsmev.testutils.Utils.createElementContent;
import static ru.mimicsmev.testutils.Utils.marshalToString;
import static ru.mimicsmev.testutils.Utils.unmarshal;
//import static ru.mimicsmev.testutils.Utils.unmarshal;

@ExtendWith(MockitoExtension.class)
class SendRequestCreatorTest {
    @Mock
    private ReqSendRequestRepository sendRequestRepository;
    @Mock
    private ReqAttachmentRepository reqAttachmentRepository;


    @Test
    void createResponse() throws JAXBException, ParserConfigurationException, IOException, SAXException, SmevInvalidContentException {
        MimicProperties mimicProperties = new MimicProperties();
        String receiptMnemonic = "my-mnemonic";
        String receiptMnemonicDesc = "my-mnemonic-desc";
        mimicProperties.getInternalVs().setMnemonic(receiptMnemonic);
        mimicProperties.getInternalVs().setMnemonicDesc(receiptMnemonicDesc);
        String msgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("v1");
        SendRequestCreator sendRequestCreator = new SendRequestCreator(jaxb2Marshaller, sendRequestRepository, reqAttachmentRepository, mimicProperties);


        SendRequestRequest sendRequestRequest = new SendRequestRequest();

        SenderProvidedRequestData senderProvidedRequestData = new SenderProvidedRequestData();
        MessagePrimaryContent messagePrimaryContent = new MessagePrimaryContent();
        messagePrimaryContent.setAny(createElementContent(marshalToString(testContentRequest)));
        senderProvidedRequestData.setMessagePrimaryContent(messagePrimaryContent);
        senderProvidedRequestData.setMessageID(msgId);

        AttachmentContentList attachmentContentList = new AttachmentContentList();
        AttachmentHeaderList attachmentHeaderList = new AttachmentHeaderList();
        AttachmentContentType attachmentContentTypeOne = new AttachmentContentType();
        attachmentContentTypeOne.setContent(createDataHandler("attachmentContentTypeOne".getBytes()));
        attachmentContentTypeOne.setId("attachmentContentTypeOne.zip");
        attachmentContentList.getAttachmentContent().add(attachmentContentTypeOne);

        AttachmentHeaderType attachmentHeaderTypeOne = new AttachmentHeaderType();
        attachmentHeaderTypeOne.setMimeType("application/zip");
        attachmentHeaderTypeOne.setContentId("attachmentContentTypeOne.zip");
        attachmentHeaderList.getAttachmentHeader().add(attachmentHeaderTypeOne);
        senderProvidedRequestData.setAttachmentHeaderList(attachmentHeaderList);
        sendRequestRequest.setAttachmentContentList(attachmentContentList);
        sendRequestRequest.setSenderProvidedRequestData(senderProvidedRequestData);

        ArgumentCaptor<ReqSendRequest> sendRequestArgumentCaptor = ArgumentCaptor.forClass(ReqSendRequest.class);
        ArgumentCaptor<List<ReqAttachment>> attachmentArgumentCaptor = ArgumentCaptor.forClass(List.class);
        SendRequestResponse sendRequestResponse = sendRequestCreator.createResponse(sendRequestRequest);

        verify(sendRequestRepository).save(sendRequestArgumentCaptor.capture());
        verify(reqAttachmentRepository).saveAll(attachmentArgumentCaptor.capture());

        ReqSendRequest sendRequest = sendRequestArgumentCaptor.getValue();
        assertAll(
                "Group assert ReqSendRequest",
                () -> assertEquals(msgId, sendRequest.getMsgId()),
                () -> assertEquals(testContentRequest, unmarshal(sendRequest.getContent())),
                () -> assertEquals(ReqStatus.RECEIVED, sendRequest.getReqStatus())
        );

        assertEquals(1, attachmentArgumentCaptor.getValue().size());
        ReqAttachment attachment = attachmentArgumentCaptor.getValue().get(0);
        assertAll(
                "Group assert ReqAttachment",
                () -> assertEquals("attachmentContentTypeOne.zip", attachment.getAttachName()),
                () -> assertEquals("attachmentContentTypeOne", new String(attachment.getAttachBlob()))
        );

        AttachmentContentType attachmentContentTypeTwo = new AttachmentContentType();
        attachmentContentTypeTwo.setContent(createDataHandler("attachmentContentTypeOne".getBytes()));
        attachmentContentTypeTwo.setId("attachmentContentTypeOne.zip");
        attachmentContentList.getAttachmentContent().add(attachmentContentTypeTwo);

        AttachmentHeaderType attachmentHeaderTypeTwo = new AttachmentHeaderType();
        attachmentHeaderTypeTwo.setMimeType("application/zip");
        attachmentHeaderTypeTwo.setContentId("attachmentContentTypeOne.zip");
        attachmentHeaderList.getAttachmentHeader().add(attachmentHeaderTypeTwo);
        senderProvidedRequestData.setAttachmentHeaderList(attachmentHeaderList);
        sendRequestRequest.setAttachmentContentList(attachmentContentList);
        sendRequestRequest.setSenderProvidedRequestData(senderProvidedRequestData);

        sendRequestResponse = sendRequestCreator.createResponse(sendRequestRequest);
        verify(reqAttachmentRepository, times(2)).saveAll(attachmentArgumentCaptor.capture());
        assertEquals(2, attachmentArgumentCaptor.getValue().size());
    }

    DataHandler createDataHandler(byte[] bos) {
        DataSource source = new ByteArrayDataSource(bos, "application/zip");
        return new DataHandler(source);
    }
}