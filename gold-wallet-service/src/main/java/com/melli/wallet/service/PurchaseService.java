package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: PurchaseService
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */

public interface PurchaseService {
    PurchaseResponse buy(ChannelEntity channel, String uniqueIdentifier, String amount, String price, String walletAccountNumber, String signData, String dataForCheckInVerify, String additionalData, String merchantId, String nationalCode, String commission, String currency, String ip, String commissionCurrency) throws InternalServiceException;
    PurchaseResponse sell(ChannelEntity channel, String uniqueIdentifier, String amount, String price, String walletAccountNumber, String signData, String dataForCheckInVerify, String additionalData, String merchantId, String nationalCode, String commission, String currency, String ip, String commissionCurrency) throws InternalServiceException;
    PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uniqueIdentifier, String channelIp) throws InternalServiceException;
}
