package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqLogRequest;

@Repository
public interface ReqLogRequestRepository extends JpaRepository<ReqLogRequest, Long> {

}