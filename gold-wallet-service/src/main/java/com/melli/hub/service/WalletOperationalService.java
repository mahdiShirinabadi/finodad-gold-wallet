package com.melli.hub.service;


import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.wallet.CreateWalletResponse;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

/**
 * @author ShirinAbadi.Mahdi on 6/18/2022
 * @project wallet-api
 */
public interface WalletOperationalService {

    CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletType, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException;
    BaseResponse deactivateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse deleteWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse activateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
}
