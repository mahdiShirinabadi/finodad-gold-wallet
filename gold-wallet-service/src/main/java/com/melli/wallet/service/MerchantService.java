package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface MerchantService {

    int ACTIVE = 1;
    int DISABLED = 2;
    MerchantWalletAccountCurrencyEntity checkPermissionOnCurrency(WalletAccountCurrencyEntity walletAccountCurrencyEntity, MerchantEntity merchant) throws InternalServiceException;
    MerchantResponse getMerchant(ChannelEntity channelEntity, String currency) throws InternalServiceException;
    MerchantEntity findById(int id);
    void save(MerchantEntity merchant);
    void clearAllCache();
}
