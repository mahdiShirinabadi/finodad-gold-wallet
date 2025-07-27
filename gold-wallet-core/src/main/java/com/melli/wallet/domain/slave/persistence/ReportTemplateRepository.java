package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportTemplateEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTemplateRepository extends CrudRepository<ReportTemplateEntity, Long> {
    ReportTemplateEntity findByName(String name);
} 