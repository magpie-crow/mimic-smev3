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
import ru.mimicsmev.dao.entity.ReqGetResponse;
import ru.mimicsmev.dao.entity.ReqStatus;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetResponseRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.service.creator.GetResponseCreator;

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
class GetResponseDAOTest {

    @Mock
    private ReqAttachmentRepository reqAttachmentRepository;
    @Mock
    private ReqGetResponseRepository getResponseRepository;
    @Mock
    private VsListRepository vsListRepository;
    @Mock
    private GetResponseCreator getResponseCreator;

    @Test
    @DisplayName("Getting one record GetResponse")
    void get() throws JAXBException {
        GetResponseDAO getResponseDAO = new GetResponseDAO(reqAttachmentRepository, getResponseRepository, vsListRepository, getResponseCreator);

        ReqGetResponse getResponse = new ReqGetResponse();
        getResponse.setReqStatus(ReqStatus.NEW);
        String rootTag = "TestTag";
        String msgId = UUID.randomUUID().toString();
        String originalMsgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");
        getResponse.setRootTag(rootTag);
        getResponse.setMsgId(msgId);
        getResponse.setOriginalMsgId(originalMsgId);
        getResponse.setContent(marshalToString(testContentRequest));
        List<ReqAttachment> attachmentList = new ArrayList<>();
        ReqAttachment reqAttachment = new ReqAttachment();
        reqAttachment.setAttachBlob("123123123".getBytes());
        reqAttachment.setAttachRow("123123123");
        reqAttachment.setAttachName("file-name");
        attachmentList.add(reqAttachment);

        Mockito.when(reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(1L, ReqType.GetResponse)).thenReturn(attachmentList);
        Mockito.when(getResponseRepository.findById(1L)).thenReturn(Optional.of(getResponse));
        GetApiModel<ReqGetResponse> requestGetApiModel = getResponseDAO.get(1);
        assertNotNull(requestGetApiModel);
        assertAll(
                "Grouped Assertions of GetApiModel<ReqGetRequest>",
                () -> assertEquals(1, requestGetApiModel.getAttachments().size()),
                () -> assertEquals("123123123", requestGetApiModel.getAttachments().get(0).getAttachRow()),
                () -> assertNotNull(requestGetApiModel.getEntity()),
                () -> assertEquals(rootTag, requestGetApiModel.getEntity().getRootTag()),
                () -> assertEquals(msgId, requestGetApiModel.getEntity().getMsgId()),
                () -> assertEquals(originalMsgId, requestGetApiModel.getEntity().getOriginalMsgId()),
                () -> assertEquals(testContentRequest, unmarshal(requestGetApiModel.getEntity().getContent()))

        );
    }

    @Test
    @DisplayName("Add new GetResponse")
    void addGetResponse() throws Exception {
        GetResponseDAO getResponseDAO = new GetResponseDAO(reqAttachmentRepository, getResponseRepository, vsListRepository, getResponseCreator);
        String rootTag = "TestTag";
        String msgId = UUID.randomUUID().toString();
        String originalMsgId = UUID.randomUUID().toString();
        TestContentRequest testContentRequest = new TestContentRequest(1L, "sender-id");

        PostApiModel postApiModel = new PostApiModel();
        postApiModel.setMsgId(msgId);
        postApiModel.setRootTag(rootTag);
        postApiModel.setOriginalMsgId(originalMsgId);
        postApiModel.setContent(new String(Base64.getEncoder().encode(marshalToString(testContentRequest).getBytes(StandardCharsets.UTF_8))));
        List<PostApiModel.Attach> attachList = new ArrayList<>();
        PostApiModel.Attach attach = new PostApiModel.Attach();
        attach.setFileName("test");
        attach.setBase64String(new String(Base64.getEncoder().encode("123123".getBytes(StandardCharsets.UTF_8))));
        attachList.add(attach);
        postApiModel.setAttachments(attachList);

        VsList vsList = new VsList();
        vsList.setRootTag("TestTag");

        ArgumentCaptor<ReqGetResponse> getRequestArgumentCaptor = ArgumentCaptor.forClass(ReqGetResponse.class);
        ArgumentCaptor<List<ReqAttachment>> reqAttachmentArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.when(vsListRepository.findVsListByRootTag(rootTag)).thenReturn(vsList);
        getResponseDAO.addGetResponse(postApiModel);

        Mockito.verify(reqAttachmentRepository).saveAll(reqAttachmentArgumentCaptor.capture());
        Mockito.verify(getResponseRepository).save(getRequestArgumentCaptor.capture());

        ReqGetResponse getRequest = getRequestArgumentCaptor.getValue();
        assertAll(
                "Grouped Assertions of ReqGetRequest",
                () -> assertEquals(rootTag, getRequest.getRootTag()),
                () -> assertEquals(msgId, getRequest.getMsgId()),
                () -> assertEquals(originalMsgId, getRequest.getOriginalMsgId()),
                () -> assertEquals(testContentRequest, unmarshal(getRequest.getContent())),
                () -> assertEquals(ReqStatus.NEW, getRequest.getReqStatus())
        );

        ReqAttachment attachment = reqAttachmentArgumentCaptor.getValue().get(0);
        assertAll(
                "Grouped Assertions of ReqAttachment",
                () -> assertEquals("test", attachment.getAttachName()),
                () -> assertEquals("123123", attachment.getAttachRow()),
                () -> assertNotNull(attachment.getAttachBlob())
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

        ArgumentCaptor<ReqGetResponse> getRequestArgumentCaptorTwo = ArgumentCaptor.forClass(ReqGetResponse.class);
        ArgumentCaptor<List<ReqAttachment>> reqAttachmentArgumentCaptorTwo = ArgumentCaptor.forClass(List.class);

        getResponseDAO.addGetResponse(postApiModel);

        Mockito.verify(reqAttachmentRepository, Mockito.times(2)).saveAll(reqAttachmentArgumentCaptorTwo.capture());
        Mockito.verify(getResponseRepository, Mockito.times(2)).save(getRequestArgumentCaptorTwo.capture());

        assertEquals(2, reqAttachmentArgumentCaptorTwo.getValue().size());
    }
}