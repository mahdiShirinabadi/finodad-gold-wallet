package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.CreateCollateralObjectDTO;
import com.melli.wallet.domain.dto.ReleaseCollateralObjectDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: CollateralOperationService
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
public interface CollateralOperationService {

    UuidResponse generateUniqueIdentifier(ChannelEntity channelEntity, String nationalCode, String quantity, String currency, String accountNumber) throws InternalServiceException;
    CreateCollateralResponse create(CreateCollateralObjectDTO createCollateralObjectDTO) throws InternalServiceException;
    void release(ReleaseCollateralObjectDTO releaseCollateralObjectDTO) throws InternalServiceException;
    void inquiry(ChannelEntity channelEntity, String uniqueIdentifier) throws InternalServiceException;
}
