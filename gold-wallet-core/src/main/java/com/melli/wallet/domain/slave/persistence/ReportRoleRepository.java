package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportRoleEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Class Name: ReportProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ReportRoleRepository extends CrudRepository<ReportRoleEntity, Long> {
    Optional<ReportRoleEntity> findByName(String name);
} 