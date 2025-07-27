package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportResourceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Class Name: ReportProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ReportResourceRepository extends CrudRepository<ReportResourceEntity, Long> {
    List<ReportResourceEntity> findAll();
    ReportResourceEntity findByName(String name);
} 