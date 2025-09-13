package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;


public interface ChannelOperationService {
    WalletBalanceResponse getBalance(ChannelEntity channelEntity) throws InternalServiceException;
    ReportTransactionResponse report(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;
}
