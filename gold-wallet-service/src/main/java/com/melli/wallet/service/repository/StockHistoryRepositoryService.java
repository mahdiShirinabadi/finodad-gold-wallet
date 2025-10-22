package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.StockHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface StockHistoryRepositoryService {

    Page<StockHistoryEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
}
