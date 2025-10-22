package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.StockHistoryEntity;
import com.melli.wallet.domain.response.stock.StockHistoryListResponse;
import com.melli.wallet.domain.response.stock.StockHistoryObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.StockHistoryOperationService;
import com.melli.wallet.service.repository.StockHistoryRepositoryService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Class Name: StockHistoryOperationServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of stock history operations
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StockHistoryOperationServiceImplementation implements StockHistoryOperationService {

    private final StockHistoryRepositoryService stockHistoryRepositoryService;
    private final Helper helper;

    @Override
    public StockHistoryListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start list stock histories for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        // Get pagination settings
        Pageable pageable = getPageableConfig(searchCriteria);

        // Get stock histories with specification and pagination
        Page<StockHistoryEntity> stockHistoryPage = stockHistoryRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        // Convert to response objects
        List<StockHistoryObject> stockHistoryObjectList = stockHistoryPage.getContent().stream()
                .map(this::convertToStockHistoryObject)
                .toList();

        log.info("finish list stock histories, found {} stock histories", stockHistoryObjectList.size());

        return helper.fillStockHistoryListResponse(stockHistoryObjectList, stockHistoryPage);
    }

    private Pageable getPageableConfig(Map<String, String> searchCriteria) {
        int page = Integer.parseInt(searchCriteria.getOrDefault("page", "0"));
        int size = Integer.parseInt(searchCriteria.getOrDefault("size", "10"));
        return PageRequest.of(page, size);
    }

    private StockHistoryObject convertToStockHistoryObject(StockHistoryEntity stockHistoryEntity) {
        StockHistoryObject stockHistoryObject = new StockHistoryObject();
        stockHistoryObject.setId(String.valueOf(stockHistoryEntity.getId()));
        stockHistoryObject.setStockId(String.valueOf(stockHistoryEntity.getStockEntity().getId()));
        stockHistoryObject.setTransactionId(String.valueOf(stockHistoryEntity.getTransactionEntity().getId()));
        stockHistoryObject.setAmount(stockHistoryEntity.getAmount().toString());
        stockHistoryObject.setType(stockHistoryEntity.getType());
        stockHistoryObject.setBalance(stockHistoryEntity.getBalance().toString());
        stockHistoryObject.setCreatedAt(stockHistoryEntity.getCreatedAt());
        stockHistoryObject.setUpdatedAt(stockHistoryEntity.getUpdatedAt());
        return stockHistoryObject;
    }
}
