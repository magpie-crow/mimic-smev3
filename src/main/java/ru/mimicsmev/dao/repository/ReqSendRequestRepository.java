package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqSendRequest;

@Repository
public interface ReqSendRequestRepository extends JpaRepository<ReqSendRequest, Long> {
}