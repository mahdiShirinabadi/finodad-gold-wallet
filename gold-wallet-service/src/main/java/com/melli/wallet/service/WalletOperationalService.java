package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * @author ShirinAbadi.Mahdi on 6/18/2022
 * @project wallet-api
 */
public interface WalletOperationalService {

    CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletType, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException;
    CreateWalletResponse get(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException;
    BaseResponse deactivateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse deleteWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
    BaseResponse activateWallet(ChannelEntity channel, String walletId, String ip) throws InternalServiceException;
}
