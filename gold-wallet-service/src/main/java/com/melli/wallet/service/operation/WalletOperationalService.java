package com.melli.wallet.service.operation;


import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.SettingGeneralEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author ShirinAbadi.Mahdi on 6/18/2022
 * @project wallet-api
 */
public interface WalletOperationalService {

    default Pageable getPageableConfig(SettingGeneralRepositoryService settingService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_SIZE);
        return PageRequest.of(page == null ? Integer.parseInt(settingPage.getValue()) : page, size == null ? Integer.parseInt(settingSize.getValue()) : size);
    }

    CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletType, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException;
    CreateWalletResponse get(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException;
    BaseResponse<ObjectUtils.Null> deactivateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse<ObjectUtils.Null> deleteWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse<ObjectUtils.Null> activateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    WalletEntity findUserWallet(String nationalCode) throws InternalServiceException;

}
