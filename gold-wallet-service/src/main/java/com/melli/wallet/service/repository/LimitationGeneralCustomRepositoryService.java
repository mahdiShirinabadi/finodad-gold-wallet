package com.melli.wallet.service.repository;


import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.limitation.GeneralCustomLimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LimitationGeneralCustomRepositoryService {
    
    default Pageable getPageableConfig(SettingGeneralRepositoryService settingService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_SIZE);
        return PageRequest.of(page == null ? Integer.parseInt(settingPage.getValue()) : page, size == null ? Integer.parseInt(settingSize.getValue()) : size);
    }
    
    String getSetting(ChannelEntity channelEntity, String limitationGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException;
    void create(ChannelEntity channelEntity, Long settingGeneralId, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity, String value, String additionalDaya) throws InternalServiceException;
    void update(ChannelEntity channelEntity, String limitationGeneralId, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity, String value, String additionalData) throws InternalServiceException;
    List<LimitationGeneralCustomEntity> getSetting(LimitationGeneralEntity limitationGeneralEntity);
    GeneralCustomLimitationListResponse getGeneralCustomLimitationList(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;
}
