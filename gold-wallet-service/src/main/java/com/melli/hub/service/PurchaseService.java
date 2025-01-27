package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.response.purchase.PurchaseResponse;
import com.melli.hub.domain.response.purchase.PurchaseTrackResponse;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: PurchaseService
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */

public interface PurchaseService {
    PurchaseResponse purchase(ChannelEntity channel, String uniqueIdentifier, String amount, String price, String account, String signData, String dataForCheckInVerify, String additionalData, String merchantId, String nationalCode) throws InternalServiceException;
    PurchaseResponse verify(ChannelEntity channel, String traceId, String customerIp) throws InternalServiceException;
    PurchaseResponse reverse(ChannelEntity channel, String traceId, String channelIp) throws InternalServiceException;
    PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uid, String channelIp) throws InternalServiceException;
    void reverseWithoutUser(String traceNumber, String ip) throws InternalServiceException;
}
