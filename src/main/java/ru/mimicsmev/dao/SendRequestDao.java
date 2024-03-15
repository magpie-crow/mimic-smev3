package ru.mimicsmev.dao;


import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqSendRequest;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.repository.ReqAttachmentRepository;
import ru.mimicsmev.dao.repository.ReqSendRequestRepository;
import ru.mimicsmev.exception.AppException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class SendRequestDao {

    private final ReqSendRequestRepository sendRequestRepository;
    private final ReqAttachmentRepository reqAttachmentRepository;

    public SendRequestDao(ReqSendRequestRepository sendRequestRepository, ReqAttachmentRepository reqAttachmentRepository) {
        this.sendRequestRepository = sendRequestRepository;
        this.reqAttachmentRepository = reqAttachmentRepository;
    }

    public Page<ReqSendRequest> getList(int pageSize, int pageNumber) {
        return sendRequestRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("id").descending()));
    }

    @SneakyThrows
    public GetApiModel<ReqSendRequest> get(long id) {
        GetApiModel<ReqSendRequest> requestGetApiModel = new GetApiModel<>();
        ReqSendRequest reqGetRequest = sendRequestRepository.findById(id).orElseThrow(() -> new AppException("Unable find id: %d".formatted(id)));
        requestGetApiModel.setEntity(reqGetRequest);
        List<ReqAttachment> reqAttachmentList = reqAttachmentRepository.findReqAttachmentsByRefIdAndReqType(id, ReqType.SendRequest);
        if (!reqAttachmentList.isEmpty()) {
            List<GetApiModel.Attach> attaches = new ArrayList<>();
            for (ReqAttachment reqAttachment : reqAttachmentList) {
                GetApiModel.Attach attach = new GetApiModel.Attach();
                attach.setFileName(reqAttachment.getAttachName());
                attach.setBase64ByteArray(new String(Base64.getEncoder().encode(reqAttachment.getAttachBlob())));
                attaches.add(attach);
            }
            requestGetApiModel.setAttachments(attaches);
        }
        return requestGetApiModel;
    }
}
