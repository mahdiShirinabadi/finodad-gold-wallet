package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportStatusRepository extends CrudRepository<ReportStatusEntity, Long> {

    ReportStatusEntity findByCode(String code);

    Page<ReportStatusEntity> findAll(Specification<ReportStatusEntity> spec, Pageable pageable);

    Optional<ReportStatusEntity> findByPersianDescription(String persianDescription);

} 