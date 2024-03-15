package ru.mimicsmev.dao;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mimicsmev.dao.content.TestContentRequest;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqSendRequest;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqSendRequestRepository;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.mimicsmev.testutils.Utils.marshalToString;
import static ru.mimicsmev.testutils.Utils.unmarshal;

@ExtendWith(MockitoExtension.class)
class SendRequestDaoTest {
    @Mock
    private ReqSendRequestRepository sendRequestRepository;
    @Mock
    private ReqAttachmentRepository reqAttachmentRepository;

    @Test
    @DisplayName("Getting one record ReqSendRequest")
    void get() throws JAXBException {
        SendRequestDao sendRequestDao = new SendRequestDao(sendRequestRepository, reqAttachmentRepository);
        ReqSendRequest sendRequest = new ReqSendRequest();
        sendRequest.setReqStatus(ReqStatus.NEW);
        sendRequest.setId(1L);
        String rootTag = "TestTag";
        String msgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");
        sendRequest.setRootTag(rootTag);
        sendRequest.setMsgId(msgId);
        sendRequest.setContent(marshalToString(testContentRequest));
        List<ReqAttachment> attachmentList = new ArrayList<>();
        ReqAttachment reqAttachment = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123".getBytes());
        reqAttachment.setAttachRow("123123123");
        reqAttachment.setAttachName("file-name");
        attachmentList.add(reqAttachment);

        Mockito.when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.SendRequest)).thenReturn(attachmentList);
        Mockito.when(sendRequestRepository.findById(1L)).thenReturn(Optional.of(sendRequest));
        GetApiModel<ReqSendRequest> requestGetApiModel = sendRequestDao.get(1);
        assertNotNull(requestGetApiModel);
        assertAll(
                "Grouped Assertions of GetApiModel<ReqGetRequest>",
                () -> assertEquals(1, requestGetApiModel.getAttachments().size()),
                () -> assertEquals("123123123", new String(Base64.getDecoder().decode(requestGetApiModel.getAttachments().get(0).getBase64ByteArray()))),
                () -> assertNotNull(requestGetApiModel.getEntity()),
                () -> assertEquals(rootTag, requestGetApiModel.getEntity().getRootTag()),
                () -> assertEquals(msgId, requestGetApiModel.getEntity().getMsgId()),
                () -> assertEquals(testContentRequest, unmarshal(requestGetApiModel.getEntity().getContent()))

        );
    }
}