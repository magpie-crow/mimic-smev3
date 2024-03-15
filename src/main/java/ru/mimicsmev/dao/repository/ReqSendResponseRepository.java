package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqSendResponse;

@Repository
public interface ReqSendResponseRepository extends JpaRepository<ReqSendResponse, Long> {
}