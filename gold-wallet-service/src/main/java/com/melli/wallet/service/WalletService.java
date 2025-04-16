package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

public interface WalletService {

    int ACTIVE = 1;
    int DISABLED = 2;

    public String WALLET_MERCHANT_PREFIX = "7";
    public String WALLET_CHANNEL_PREFIX = "8";
    public String WALLET_NORMAL_PREFIX = "9";

    short LEVEL_1 = 1;
    short LEVEL_2 = 2;

    WalletEntity findByNationalCodeAndWalletTypeId(String nationalCode, long walletTypeEntityId);

    WalletEntity findById(Long walletId) throws InternalServiceException;

    void save(WalletEntity wallet);

    List<WalletEntity> findAllByStatus(int status);

    void clearAllCache();
}
