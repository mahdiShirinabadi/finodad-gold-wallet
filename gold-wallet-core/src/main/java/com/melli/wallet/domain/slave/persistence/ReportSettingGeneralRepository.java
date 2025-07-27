package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportSettingGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportSettingGeneralRepository extends CrudRepository<ReportSettingGeneralEntity, Long> {

    ReportSettingGeneralEntity findByNameAndEndTimeIsNull(String name);

    Page<ReportSettingGeneralEntity> findAll(Specification<ReportSettingGeneralEntity> spec, Pageable pageable);
} 