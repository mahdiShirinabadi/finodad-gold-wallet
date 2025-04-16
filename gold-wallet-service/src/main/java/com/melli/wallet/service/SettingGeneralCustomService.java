package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

public interface SettingGeneralCustomService {
    String getSetting(ChannelEntity channelEntity, String settingGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException;
    void save(ChannelEntity channelEntity, SettingGeneralCustomEntity settingGeneralCustomEntity) throws InternalServiceException;
    List<SettingGeneralCustomEntity> getSetting(SettingGeneralEntity settingGeneralEntity);
}
