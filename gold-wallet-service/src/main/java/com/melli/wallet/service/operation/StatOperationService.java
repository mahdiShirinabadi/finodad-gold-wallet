package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.stat.StatBuyListResponse;
import com.melli.wallet.domain.response.stat.StatSellListResponse;
import com.melli.wallet.domain.response.stat.StatWalletListResponse;
import com.melli.wallet.domain.response.stat.StatPerson2PersonListResponse;
import com.melli.wallet.domain.response.stat.StatPhysicalCashOutListResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

/**
 * Class Name: StatOperationService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Service interface for statistics operations
 */
public interface StatOperationService {

    StatBuyListResponse getBuyStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;

    StatSellListResponse getSellStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;

    StatWalletListResponse getWalletStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;

    StatPerson2PersonListResponse getPerson2PersonStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;

    StatPhysicalCashOutListResponse getPhysicalCashOutStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;
}
