package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportShahkarInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportShahkarInfoRepository extends CrudRepository<ReportShahkarInfoEntity, Long> {

    Optional<ReportShahkarInfoEntity> findTopByMobileAndNationalCodeAndIsMatchOrderByIdDesc(String nationalCode, String mobile, Boolean isMatch);

    Page<ReportShahkarInfoEntity> findAll(Specification<ReportShahkarInfoEntity> spec, Pageable pageable);
} 