package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.merchant.MerchantBalanceCalculationResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;


public interface MerchantOperationService {

    void updateStatus(ChannelEntity channelEntity, String merchantId, String status) throws InternalServiceException;
    WalletBalanceResponse getBalance(ChannelEntity channelEntity, String merchantId) throws InternalServiceException;
    String increaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException;
    String decreaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException;
    ReportTransactionResponse report(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;
    MerchantBalanceCalculationResponse calculateBalanceFromTransactions(ChannelEntity channelEntity, String merchantId, String currency) throws InternalServiceException;
}
