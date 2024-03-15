package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqType;

import java.util.List;

@Repository
public interface ReqAttachmentRepository extends JpaRepository<ReqAttachment, Long> {
    List<ReqAttachment> findReqAttachmentsByRefIdAndReqType(Long refId, ReqType type);
}