package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface MerchantOperationService {

    void updateStatus(ChannelEntity channelEntity, String merchantId, String status) throws InternalServiceException;
    WalletBalanceResponse getBalance(ChannelEntity channelEntity, String currency) throws InternalServiceException;
    String increaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException;
    String decreaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException;
}
