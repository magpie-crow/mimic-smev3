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
import ru.mimicsmev.dao.repository.ReqGetRequestRepository;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.AppException;
import ru.mimicsmev.service.creator.GetRequestCreator;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static ru.mimicsmev.utils.Utils.trimValue;

@Service
@Slf4j
public class GetRequestDAO {

    private final ReqAttachmentRepository reqAttachmentRepository;
    private final ReqGetRequestRepository getRequestRepository;
    private final VsListRepository vsListRepository;
    private final GetRequestCreator getRequestCreator;

    public GetRequestDAO(ReqAttachmentRepository reqAttachmentRepository, ReqGetRequestRepository getRequestRepository, VsListRepository vsListRepository, GetRequestCreator getRequestCreator) {
        this.reqAttachmentRepository = reqAttachmentRepository;
        this.getRequestRepository = getRequestRepository;
        this.vsListRepository = vsListRepository;
        this.getRequestCreator = getRequestCreator;
    }

    public Page<ReqGetRequest> getList(int pageSize, int pageNumber) {
        return getRequestRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("id"))));
    }

    @SneakyThrows
    public GetApiModel<ReqGetRequest> get(long id) {
        GetApiModel<ReqGetRequest> requestGetApiModel = new GetApiModel<>();
        ReqGetRequest reqGetRequest = getRequestRepository.findById(id).orElseThrow(() -> new AppException("Unable find id: %d".formatted(id)));
        requestGetApiModel.setEntity(reqGetRequest);
        List<ReqAttachment> reqAttachmentList = reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(id, ReqType.GetRequest);
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
    public ReqGetRequest addGetRequest(PostApiModel model) throws AppException {
        try {
            ReqGetRequest getRequest = new ReqGetRequest();
            getRequest.setReqStatus(ReqStatus.NEW);
            String msgId = trimValue(model.getMsgId());
            String rootTag = trimValue(model.getRootTag());
            getRequest.setMsgId(StringUtils.hasLength(msgId) ? msgId : UUID.randomUUID().toString());
            VsList vsList = vsListRepository.findVsListByRootTag(rootTag);
            if (Objects.isNull(vsList)) {
                throw new AppException(String.format("Unable find VS by root tag '%s'", rootTag));
            }
            getRequest.setRootTag(rootTag);

            String xmlContentRow = new String(Base64.getDecoder().decode(model.getContent()), StandardCharsets.UTF_8);
            getRequest.setContent(xmlContentRow);
            getRequestRepository.save(getRequest);
            if (!model.getAttachments().isEmpty()) {
                List<ReqAttachment> getAttachmentList = new ArrayList<>();
                for (PostApiModel.Attach attach : model.getAttachments()) {
                    ReqAttachment attachment = AttachmentMapper.INSTANCE.map(attach, getRequest.getId(), ReqType.GetRequest);
                    getAttachmentList.add(attachment);
                }
                reqAttachmentRepository.saveAll(getAttachmentList);
            }
            String reqRow = getRequestCreator.marshalToString(getRequestCreator.buildRequest(getRequest));
            getRequest.setReqRow(reqRow);
            return getRequest;
        } catch (Exception e) {
            log.error("Failed add new request: ", e);
            throw new AppException(e);
        }

    }
}
