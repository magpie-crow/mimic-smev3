package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.ReqGetResponse;

@Repository
public interface ReqGetResponseRepository extends JpaRepository<ReqGetResponse, Long> {

    @Query(
            value = "select * from req_get_response where status= cast(:status as req_status)  for update skip locked limit 1",
            nativeQuery = true
    )
    ReqGetResponse getOneByStatus(String status);

    @Query(
            value = "select * from req_get_response where status= cast(:status as req_status) and root_tag=:rootTag for update skip locked limit 1",
            nativeQuery = true
    )
    ReqGetResponse getOneByStatusAndRootTag(String status, String rootTag);
}