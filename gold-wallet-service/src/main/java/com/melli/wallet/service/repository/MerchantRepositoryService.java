package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface MerchantRepositoryService {

    int ACTIVE = 1;
    int DISABLED = 2;
    MerchantWalletAccountCurrencyEntity checkPermissionOnCurrency(WalletAccountCurrencyEntity walletAccountCurrencyEntity, MerchantEntity merchant) throws InternalServiceException;
    MerchantResponse getMerchant(ChannelEntity channelEntity, String currency) throws InternalServiceException;
    MerchantEntity findById(int id);
    void save(MerchantEntity merchant);
    void clearAllCache();
    MerchantEntity findMerchant(String merchantId) throws InternalServiceException;
    WalletAccountEntity findMerchantWalletAccount(
            MerchantEntity merchant, WalletAccountCurrencyEntity currencyEntity) throws InternalServiceException;

}
