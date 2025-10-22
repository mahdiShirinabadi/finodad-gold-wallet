package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.stock.StockHistoryListResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

/**
 * Class Name: StockHistoryOperationService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Service interface for stock history operations
 */
public interface StockHistoryOperationService {

    StockHistoryListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;
}
