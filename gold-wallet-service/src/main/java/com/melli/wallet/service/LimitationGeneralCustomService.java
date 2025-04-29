package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

public interface LimitationGeneralCustomService {
    String getSetting(ChannelEntity channelEntity, String limitationGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException;
    void create(ChannelEntity channelEntity, String settingGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity, String value, String additionalDaya) throws InternalServiceException;
    List<LimitationGeneralCustomEntity> getSetting(LimitationGeneralEntity limitationGeneralEntity);
}
