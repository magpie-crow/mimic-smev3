package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqGetRequest;

@Repository
public interface ReqGetRequestRepository extends JpaRepository<ReqGetRequest, Long> {

    @Query(
            value = "select * from req_get_request where status= cast(:status as req_status)  for update skip locked limit 1",
            nativeQuery = true
    )
    ReqGetRequest getOneByStatus(String status);

    @Query(
            value = "select * from req_get_request where status= cast(:status as req_status) and root_tag=:rootTag for update skip locked limit 1",
            nativeQuery = true
    )
    ReqGetRequest getOneByStatusAndRootTag(String status, String rootTag);
}