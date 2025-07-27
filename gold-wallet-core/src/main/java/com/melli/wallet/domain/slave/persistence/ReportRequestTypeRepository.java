package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportRequestTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRequestTypeRepository extends CrudRepository<ReportRequestTypeEntity, Long> {
    ReportRequestTypeEntity findByName(String name);
} 