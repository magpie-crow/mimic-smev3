package ru.mimicsmev.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mimicsmev.dao.entity.VsList;

@Repository
public interface VsListRepository extends JpaRepository<VsList, Long> {
    VsList findVsListByRootTag(String rootTag);
}