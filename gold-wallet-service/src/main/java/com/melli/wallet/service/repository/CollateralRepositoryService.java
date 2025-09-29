package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.CollateralEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.response.collateral.CollateralResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface CollateralRepositoryService {

    int ACTIVE = 1;
    int DISABLED = 2;
    CollateralResponse get(ChannelEntity channelEntity, String currency) throws InternalServiceException;
    CollateralEntity findById(int id);
    void save(CollateralEntity merchant);
    void clearAllCache();
    CollateralEntity findCollateral(String merchantId) throws InternalServiceException;
    WalletAccountEntity findCollateralWalletAccount(
            CollateralEntity collateralEntity, WalletAccountCurrencyEntity currencyEntity) throws InternalServiceException;
}
