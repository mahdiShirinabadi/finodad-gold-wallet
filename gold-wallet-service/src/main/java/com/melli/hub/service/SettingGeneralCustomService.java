package com.melli.hub.service;


import com.melli.hub.domain.master.entity.*;
import com.melli.hub.exception.InternalServiceException;

public interface SettingGeneralCustomService {
    String getSetting(ChannelEntity channelEntity, String settingGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException;
    void save(SettingGeneralCustomEntity settingGeneralCustomEntity);
    SettingGeneralCustomEntity getSetting(SettingGeneralEntity settingGeneralEntity);
}
