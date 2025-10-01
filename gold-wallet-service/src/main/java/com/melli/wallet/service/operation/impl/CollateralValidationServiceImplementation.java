package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.IncreaseCollateralObjectDTO;
import com.melli.wallet.domain.dto.ReleaseCollateralObjectDTO;
import com.melli.wallet.domain.dto.SeizeCollateralObjectDTO;
import com.melli.wallet.domain.dto.SellCollateralObjectDTO;
import com.melli.wallet.domain.enumaration.CollateralStatusEnum;
import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.CollateralValidationService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Class Name: CollateralValidationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 10/1/2025
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CollateralValidationServiceImplementation implements CollateralValidationService {

    @Override
    public void checkReleaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {
        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("checkReleaseCollateral owner collateral for code ({}) is ({}) and not same with channel caller ({})", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("checkReleaseCollateral collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("checkReleaseCollateral collateral with code ({}) release before!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("checkReleaseCollateral collateral  release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getFinalBlockQuantity().compareTo(objectDTO.getQuantity()) != 0) {
            log.error("checkReleaseCollateral quantity collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("checkReleaseCollateral collateral quantity not same", StatusRepositoryService.COLLATERAL_QUANTITY_NOT_SAME, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("checkReleaseCollateral nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("checkReleaseCollateral collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    @Override
    public void checkIncreaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, IncreaseCollateralObjectDTO objectDTO) throws InternalServiceException {
        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({})", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("checkIncreaseCollateral collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("collateral with code ({}) release before!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("checkIncreaseCollateral collateral release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("checkIncreaseCollateral collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    @Override
    public void checkSeizeCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SeizeCollateralObjectDTO objectDTO) throws InternalServiceException {
        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({}) in seize", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("collateral with code ({}) release before in seize!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.SEIZE.toString())) {
            log.error("collateral with code ({}) release before in seize!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral seize before", StatusRepositoryService.COLLATERAL_SEIZE_BEFORE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    @Override
    public void checkSellCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SellCollateralObjectDTO objectDTO) throws InternalServiceException {

        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({}) in sell", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("collateral with code ({}) release before in sell!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral channel not same", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.SEIZE.toString())) {
            log.error("collateral with code ({}) must be SEIZE!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral step not valid", StatusRepositoryService.COLLATERAL_STEP_MUST_BE_SEIZE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral sell ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }

        if (objectDTO.getQuantity().compareTo(createCollateralRequestEntity.getQuantity()) > 0) {
            log.error("quantity for sell ({}) is bigger than first quantity in collateral ({})", objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("quantity is bigger than input quantity", StatusRepositoryService.COLLATERAL_QUANTITY_IS_BIGGER_THAN_BLOCK_QUANTITY, HttpStatus.OK);
        }
    }
}
