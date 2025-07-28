package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.SettingGeneralEntity;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author ShirinAbadi.Mahdi on 6/18/2022
 * @project wallet-api
 */
public interface WalletOperationalService {

    default Pageable getPageableConfig(SettingGeneralService settingService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingService.getSetting(SettingGeneralService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingService.getSetting(SettingGeneralService.SETTLE_DEFAULT_SIZE);
        return PageRequest.of(page == null ? Integer.parseInt(settingPage.getValue()) : page, size == null ? Integer.parseInt(settingSize.getValue()) : size);
    }

    CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletType, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException;
    CreateWalletResponse get(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException;
    BaseResponse deactivateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse deleteWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse activateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    void getStatement(ChannelEntity channel, Map<String, String> mapParameter, String ip) throws InternalServiceException;

}
