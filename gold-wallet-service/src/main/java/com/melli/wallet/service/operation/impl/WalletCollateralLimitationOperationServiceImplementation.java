package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.WalletCollateralLimitationOperationService;
import com.melli.wallet.service.repository.LimitationGeneralCustomRepositoryService;
import com.melli.wallet.service.repository.LimitationGeneralService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: WalletCollateralLimitationOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletCollateralLimitationOperationServiceImplementation implements WalletCollateralLimitationOperationService {


    private final LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;


    @Override
    public void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException {

        log.info("checking checkSellGeneral for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableCollateralCard = Boolean.parseBoolean(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.ENABLE_COLLATERAL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableCollateralCard)) {
            log.error("checkCollateralLimitation: ENABLE_COLLATERAL in for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel.getUsername(), walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            StringBuilder st;
            st = new StringBuilder();
            st.append("checkCollateralLimitation: account (").append( walletAccount.getAccountNumber()).append(") dont permission to collateral");
            throw new InternalServiceException(st.toString(), StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_COLLATERAL, HttpStatus.OK);
        }
    }
}
