package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.IncreaseCollateralObjectDTO;
import com.melli.wallet.domain.dto.ReleaseCollateralObjectDTO;
import com.melli.wallet.domain.dto.SeizeCollateralObjectDTO;
import com.melli.wallet.domain.dto.SellCollateralObjectDTO;
import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: CollateralValidationService
 * Author: Mahdi Shirinabadi
 * Date: 10/1/2025
 */
public interface CollateralValidationService {

    void checkReleaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException;
    void checkIncreaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, IncreaseCollateralObjectDTO objectDTO) throws InternalServiceException;
    void checkSeizeCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SeizeCollateralObjectDTO objectDTO) throws InternalServiceException;
    void checkSellCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SellCollateralObjectDTO objectDTO) throws InternalServiceException;
}
