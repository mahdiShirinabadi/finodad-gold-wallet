package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.*;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.collateral.CollateralListResponse;
import com.melli.wallet.domain.response.collateral.CollateralTrackResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

/**
 * Class Name: CollateralOperationService
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
public interface CollateralOperationService {

    UuidResponse generateUniqueIdentifier(ChannelEntity channelEntity, String nationalCode, String quantity, String currency, String accountNumber) throws InternalServiceException;
    CreateCollateralResponse create(CreateCollateralObjectDTO createCollateralObjectDTO) throws InternalServiceException;
    void release(ReleaseCollateralObjectDTO releaseCollateralObjectDTO) throws InternalServiceException;
    void increase(IncreaseCollateralObjectDTO increaseCollateralObjectDTO) throws InternalServiceException;
    void seize(SeizeCollateralObjectDTO seizeCollateralObjectDTO) throws InternalServiceException;
    void sell(SellCollateralObjectDTO sellCollateralObjectDTO) throws InternalServiceException;
    CollateralTrackResponse inquiry(ChannelEntity channelEntity, String uniqueIdentifier, String ip) throws InternalServiceException;
    WalletBalanceResponse getBalance(ChannelEntity channelEntity, String id) throws InternalServiceException;
    ReportTransactionResponse  report(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;
    CollateralListResponse list(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;
}
