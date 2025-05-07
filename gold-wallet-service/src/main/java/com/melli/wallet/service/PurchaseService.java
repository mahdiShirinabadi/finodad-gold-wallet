package com.melli.wallet.service;

import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.SellRequestDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: PurchaseService
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */

public interface PurchaseService {
    UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String price, String accountNumber, String type) throws InternalServiceException;
    PurchaseResponse buy(BuyRequestDTO buyRequestDTO) throws InternalServiceException;
    PurchaseResponse sell(SellRequestDTO sellRequestDTO) throws InternalServiceException;
    PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uniqueIdentifier, String type, String channelIp) throws InternalServiceException;
}
