package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportLimitationGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportLimitationGeneralRepository extends CrudRepository<ReportLimitationGeneralEntity, Long> {
    ReportLimitationGeneralEntity findByNameAndEndTimeIsNull(String name);
    ReportLimitationGeneralEntity findByName(String name);
    Page<ReportLimitationGeneralEntity> findAll(Specification<ReportLimitationGeneralEntity> spec, Pageable pageable);
    List<ReportLimitationGeneralEntity> findAll();
} 