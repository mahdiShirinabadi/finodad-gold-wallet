package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.StatSellEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Class Name: StatSellRepositoryService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Repository service interface for StatSellEntity operations
 */
public interface StatSellRepositoryService {

    Page<StatSellEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
}
