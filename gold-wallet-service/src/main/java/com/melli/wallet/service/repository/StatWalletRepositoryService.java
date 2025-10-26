package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.StatWalletEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Class Name: StatWalletRepositoryService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Repository service interface for StatWalletEntity operations
 */
public interface StatWalletRepositoryService {

    Page<StatWalletEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
}
