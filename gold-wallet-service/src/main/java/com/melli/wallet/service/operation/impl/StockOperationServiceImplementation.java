package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.StockEntity;
import com.melli.wallet.domain.response.stock.StockListResponse;
import com.melli.wallet.domain.response.stock.StockObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.StockOperationService;
import com.melli.wallet.service.repository.StockRepositoryService;
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
 * Class Name: StockOperationServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of stock operations
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StockOperationServiceImplementation implements StockOperationService {

    private final StockRepositoryService stockRepositoryService;
    private final Helper helper;

    @Override
    public StockListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start list stocks for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        // Get pagination settings
        Pageable pageable = getPageableConfig(searchCriteria);

        // Get stocks with specification and pagination
        Page<StockEntity> stockPage = stockRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        // Convert to response objects
        List<StockObject> stockObjectList = stockPage.getContent().stream()
                .map(this::convertToStockObject)
                .toList();

        log.info("finish list stocks, found {} stocks", stockObjectList.size());

        return helper.fillStockListResponse(stockObjectList, stockPage);
    }

    private Pageable getPageableConfig(Map<String, String> searchCriteria) {
        int page = Integer.parseInt(searchCriteria.getOrDefault("page", "0"));
        int size = Integer.parseInt(searchCriteria.getOrDefault("size", "10"));
        return PageRequest.of(page, size);
    }

    private StockObject convertToStockObject(StockEntity stockEntity) {
        StockObject stockObject = new StockObject();
        stockObject.setId(String.valueOf(stockEntity.getId()));
        stockObject.setBalance(stockRepositoryService.getBalance(stockEntity.getId()).toPlainString());
        stockObject.setCode(stockEntity.getCode());
        return stockObject;
    }
}
