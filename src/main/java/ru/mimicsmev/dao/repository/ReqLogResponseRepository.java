package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqLogResponse;

@Repository
public interface ReqLogResponseRepository extends JpaRepository<ReqLogResponse, Long> {
}