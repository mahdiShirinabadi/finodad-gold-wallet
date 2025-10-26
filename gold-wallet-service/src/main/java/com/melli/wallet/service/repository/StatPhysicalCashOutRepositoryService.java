package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.StatPhysicalCashOutEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Class Name: StatPhysicalCashOutRepositoryService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Repository service interface for StatPhysicalCashOutEntity operations
 */
public interface StatPhysicalCashOutRepositoryService {

    Page<StatPhysicalCashOutEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
}
