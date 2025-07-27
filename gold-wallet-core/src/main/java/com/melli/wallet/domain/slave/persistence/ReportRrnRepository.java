package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportRrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRrnRepository extends CrudRepository<ReportRrnEntity, Long> {

    @Query(value = "select r from ReportRrnEntity r where r.id=:id")
    ReportRrnEntity findById(@Param("id") long id);

    @Query(value="select uid from {h-schema}rrn where id = :id", nativeQuery= true)
    String findUidById(@Param("id") long id);

    ReportRrnEntity findByUuid(String uuid);

} 