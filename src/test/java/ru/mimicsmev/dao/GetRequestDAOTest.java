package ru.mimicsmev.dao;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mimicsmev.dao.content.TestContentRequest;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqGetRequest;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetRequestRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.service.creator.GetRequestCreator;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.mimicsmev.testutils.Utils.marshalToString;
import static ru.mimicsmev.testutils.Utils.unmarshal;

@ExtendWith(MockitoExtension.class)
class GetRequestDAOTest {
    @Mock
    private ReqAttachmentRepository reqAttachmentRepository;
    @Mock
    private ReqGetRequestRepository getRequestRepository;
    @Mock
    private VsListRepository vsListRepository;
    @Mock
    private GetRequestCreator getRequestCreator;

    @Test
    @DisplayName("Getting one record ReqGetRequest")
    void getTest() throws JAXBException {
        GetRequestDAO getRequestDAO = new GetRequestDAO(reqAttachmentRepository, getRequestRepository, vsListRepository, getRequestCreator);

        ReqGetRequest getRequest = new ReqGetRequest();
        getRequest.setReqStatus(ReqStatus.NEW);
        getRequest.setId(1L);
        String rootTag = "TestTag";
        String msgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");
        getRequest.setRootTag(rootTag);
        getRequest.setMsgId(msgId);
        getRequest.setContent(marshalToString(testContentRequest));
        List<ReqAttachment> attachmentList = new ArrayList<>();
        ReqAttachment reqAttachment = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123".getBytes());
        reqAttachment.setAttachRow("123123123");
        reqAttachment.setAttachName("file-name");
        attachmentList.add(reqAttachment);

        Mockito.when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.GetRequest)).thenReturn(attachmentList);
        Mockito.when(getRequestRepository.findById(1L)).thenReturn(Optional.of(getRequest));
        GetApiModel<ReqGetRequest> requestGetApiModel = getRequestDAO.get(1);
        assertNotNull(requestGetApiModel);
        assertAll(
                "Grouped Assertions of GetApiModel<ReqGetRequest>",
                () -> assertEquals(1, requestGetApiModel.getAttachments().size()),
                () -> assertEquals("123123123", requestGetApiModel.getAttachments().get(0).getAttachRow()),
                () -> assertNotNull(requestGetApiModel.getEntity()),
                () -> assertEquals(rootTag, requestGetApiModel.getEntity().getRootTag()),
                () -> assertEquals(msgId, requestGetApiModel.getEntity().getMsgId()),
                () -> assertEquals(testContentRequest, unmarshal(requestGetApiModel.getEntity().getContent()))

        );
    }

    @Test
    @DisplayName("Add new GetRequest")
    void addGetRequestTest() throws Exception {
        GetRequestDAO getRequestDAO = new GetRequestDAO(reqAttachmentRepository, getRequestRepository, vsListRepository, getRequestCreator);
        String rootTag = "TestTag";
        String msgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");

        PostApiModel postApiModel = new PostApiModel();
        postApiModel.setMsgId(msgId);
        postApiModel.setRootTag(rootTag);
        postApiModel.setContent(new String(Base64.getEncoder().encode(marshalToString(testContentRequest).getBytes(StandardCharsets.UTF_8))));
        List<PostApiModel.Attach> attachList = new ArrayList<>();
        PostApiModel.Attach attach = new PostApiModel.Attach();
        attach.setFileName("testName");
        attach.setBase64String(new String(Base64.getEncoder().encode("123123".getBytes(StandardCharsets.UTF_8))));
        attachList.add(attach);
        postApiModel.setAttachments(attachList);

        VsList vsList = new VsList();
        vsList.setRootTag("TestTag");

        ArgumentCaptor<ReqGetRequest> getRequestArgumentCaptor = ArgumentCaptor.forClass(ReqGetRequest.class);
        ArgumentCaptor<List<ReqAttachment>> reqAttachmentArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.when(vsListRepository.findVsListByRootTag(rootTag)).thenReturn(vsList);
        getRequestDAO.addGetRequest(postApiModel);

        Mockito.verify(reqAttachmentRepository).saveAll(reqAttachmentArgumentCaptor.capture());
        Mockito.verify(getRequestRepository).save(getRequestArgumentCaptor.capture());

        ReqGetRequest getRequest = getRequestArgumentCaptor.getValue();
        assertAll(
                "Grouped Assertions of ReqGetRequest",
                () -> assertEquals(rootTag, getRequest.getRootTag()),
                () -> assertEquals(msgId, getRequest.getMsgId()),
                () -> assertEquals(testContentRequest, unmarshal(getRequest.getContent())),
                () -> assertEquals(ReqStatus.NEW, getRequest.getReqStatus())
        );

        ReqAttachment attachment = reqAttachmentArgumentCaptor.getValue().get(0);
        assertAll(
                "Grouped Assertions of ReqAttachment",
                () -> assertEquals("testName", attachment.getAttachName()),
                () -> assertEquals("123123", attachment.getAttachRow()),
                () -> assertNotNull(attachment.getAttachBlob()),
                () -> assertEquals("testName", attachment.getAttachName())
        );

        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(attachment.getAttachBlob()));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        boolean fileName = false;
        boolean fileNameSig = false;
        int fCount = 0;
        while (Objects.nonNull(zipEntry)) {
            fCount++;
            if (fCount > 2) {
               throw new Exception("too may entries");
            }
            String fname = zipEntry.getName();
            if (fname.matches("^\\w*.xml$")) {
                fileName = true;
                assertEquals("123123", new String(zipInputStream.readAllBytes()));
            } else if (fname.matches("^\\w*.sig")) {
                fileNameSig = true;
            }
            zipEntry = zipInputStream.getNextEntry();

        }
        boolean finalFileName = fileName;
        boolean finalFileNameSig = fileNameSig;
        assertAll(
                "Zip archive contain xml file and sig",
                () -> assertTrue(finalFileName),
                () -> assertTrue(finalFileNameSig)
        );

        PostApiModel.Attach attachTwo = new PostApiModel.Attach();
        attachTwo.setFileName("test2");
        attachTwo.setBase64String(new String(Base64.getEncoder().encode("000000".getBytes(StandardCharsets.UTF_8))));
        attachList.add(attachTwo);

        ArgumentCaptor<ReqGetRequest> getRequestArgumentCaptorTwo = ArgumentCaptor.forClass(ReqGetRequest.class);
        ArgumentCaptor<List<ReqAttachment>> reqAttachmentArgumentCaptorTwo = ArgumentCaptor.forClass(List.class);

        getRequestDAO.addGetRequest(postApiModel);

        Mockito.verify(reqAttachmentRepository, Mockito.times(2)).saveAll(reqAttachmentArgumentCaptorTwo.capture());
        Mockito.verify(getRequestRepository, Mockito.times(2)).save(getRequestArgumentCaptorTwo.capture());

        assertEquals(2, reqAttachmentArgumentCaptorTwo.getValue().size());

    }
}