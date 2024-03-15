package ru.mimicsmev.dao;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.mimicsmev.dao.entity.*;
import ru.mimicsmev.dao.mapper.AttachmentMapper;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqGetResponseRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.AppException;
import ru.mimicsmev.service.creator.GetResponseCreator;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static ru.mimicsmev.utils.Utils.trimValue;

@Service
@Slf4j
public class GetResponseDAO {

    private final ReqAttachmentRepository reqAttachmentRepository;
    private final ReqGetResponseRepository getResponseRepository;
    private final VsListRepository vsListRepository;
    private final GetResponseCreator getResponseCreator;

    public GetResponseDAO(ReqAttachmentRepository reqAttachmentRepository, ReqGetResponseRepository getResponseRepository, VsListRepository vsListRepository, GetResponseCreator getResponseCreator) {
        this.reqAttachmentRepository = reqAttachmentRepository;
        this.getResponseRepository = getResponseRepository;
        this.vsListRepository = vsListRepository;
        this.getResponseCreator = getResponseCreator;
    }

    public Page<ReqGetResponse> getList(int pageSize, int pageNumber) {
        return getResponseRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("id").descending()));
    }

    @SneakyThrows
    public GetApiModel<ReqGetResponse> get(long id) {
        GetApiModel<ReqGetResponse> requestGetApiModel = new GetApiModel<>();
        ReqGetResponse reqGetRequest = getResponseRepository.findById(id).orElseThrow(() -> new AppException("Unable find id: %d".formatted(id)));
        requestGetApiModel.setEntity(reqGetRequest);
        List<ReqAttachment> reqAttachmentList = reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(id, ReqType.GetResponse);
        if (!reqAttachmentList.isEmpty()) {
            List<GetApiModel.Attach> attaches = new ArrayList<>();
            for (ReqAttachment reqAttachment : reqAttachmentList) {
                GetApiModel.Attach attach = new GetApiModel.Attach();
                attach.setFileName(reqAttachment.getAttachName());
                attach.setAttachRow(reqAttachment.getAttachRow());
                attach.setBase64ByteArray(new String(Base64.getEncoder().encode(reqAttachment.getAttachBlob())));
                attaches.add(attach);
            }
            requestGetApiModel.setAttachments(attaches);
        }
        return requestGetApiModel;
    }

    @Transactional
    public ReqGetResponse addGetResponse(PostApiModel model) throws AppException {
        try {
            ReqGetResponse getResponse = new ReqGetResponse();
            getResponse.setReqStatus(ReqStatus.NEW);
            String msgId = trimValue(model.getMsgId());
            getResponse.setMsgId(StringUtils.hasLength(msgId) ? msgId : UUID.randomUUID().toString());
            String originalMsgId = trimValue(model.getOriginalMsgId());
            getResponse.setOriginalMsgId(StringUtils.hasLength(originalMsgId) ? originalMsgId : UUID.randomUUID().toString());
            String rootTag = trimValue(model.getRootTag());
            VsList vsList = vsListRepository.findVsListByRootTag(rootTag);
            if (Objects.isNull(vsList)) {
                throw new AppException(String.format("Unable find VS by root tag '%s'", rootTag));
            }
            getResponse.setRootTag(rootTag);
            String xmlContentRow = new String(Base64.getDecoder().decode(model.getContent()), StandardCharsets.UTF_8);
            getResponse.setContent(xmlContentRow);
            getResponseRepository.save(getResponse);
            if (!model.getAttachments().isEmpty()) {
                List<ReqAttachment> getAttachmentList = new ArrayList<>();
                for (PostApiModel.Attach attach : model.getAttachments()) {
                    ReqAttachment attachment = AttachmentMapper.INSTANCE.map(attach, getResponse.getId(), ReqType.GetResponse);
                    getAttachmentList.add(attachment);
                }
                reqAttachmentRepository.saveAll(getAttachmentList);
            }
            String reqRow = getResponseCreator.marshalToString(getResponseCreator.buildResponse(getResponse));
            getResponse.setReqRow(reqRow);
            return getResponse;
        } catch (Exception e) {
            log.error("Failed add new response: ", e);
            throw new AppException(e);
        }

    }

}
