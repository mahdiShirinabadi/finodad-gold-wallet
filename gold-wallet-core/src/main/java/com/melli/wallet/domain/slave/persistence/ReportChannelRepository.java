package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportChannelEntity;
import com.melli.wallet.domain.slave.entity.ReportChannelRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Class Name: ReportProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ReportChannelRepository extends CrudRepository<ReportChannelEntity, Long> {
    ReportChannelEntity findByUsername(String username);
    Page<ReportChannelEntity> findAll(Specification<ReportChannelEntity> specification, Pageable pageRequest);
} 