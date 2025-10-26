package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.StatBuyEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Class Name: StatBuyRepositoryService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Repository service interface for StatBuyEntity operations
 */
public interface StatBuyRepositoryService {

    Page<StatBuyEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
}
