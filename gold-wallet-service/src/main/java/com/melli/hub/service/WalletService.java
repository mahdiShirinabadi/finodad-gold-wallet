package com.melli.hub.service;

import com.melli.hub.domain.master.entity.WalletEntity;
import com.melli.hub.domain.master.entity.WalletTypeEntity;
import com.melli.hub.exception.InternalServiceException;

import java.io.IOException;
import java.util.Date;
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

    void clearCache(String mobile);

    List<WalletEntity> findAllByStatus(int status);

    void clearAllCache();
}
