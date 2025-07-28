package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportLimitationGeneralCustomEntity;
import com.melli.wallet.domain.slave.entity.ReportLimitationGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportLimitationGeneralCustomRepository extends CrudRepository<ReportLimitationGeneralCustomEntity, Long> {

    List<ReportLimitationGeneralCustomEntity> findByLimitationGeneralEntityAndEndTimeIsNull(ReportLimitationGeneralEntity limitationGeneralEntity);
    List<ReportLimitationGeneralCustomEntity> findByEndTimeIsNull();
    Page<ReportLimitationGeneralCustomEntity> findAll(Specification<ReportLimitationGeneralCustomEntity> spec, Pageable pageable);
} 