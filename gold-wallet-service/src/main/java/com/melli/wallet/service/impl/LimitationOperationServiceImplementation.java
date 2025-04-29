package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.limitation.LimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: LimitationOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 4/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class LimitationOperationServiceImplementation implements LimitationOperationService {
    private final LimitationGeneralService limitationGeneralService;
    private final LimitationGeneralCustomService limitationGeneralCustomService;
    private final WalletAccountService walletAccountService;
    private final WalletService walletService;
    private final WalletTypeService walletTypeService;
    private final Helper helper;
    @Override
    public String getValue(ChannelEntity channelEntity, String limitationName, String accountNumber, String nationalCode, String ip) throws InternalServiceException {

        WalletTypeEntity walletTypeEntity = walletTypeService.getByName(WalletTypeService.NORMAL_USER);
        WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());
        walletAccountService.findByWalletAndAccount(walletEntity, accountNumber);

        WalletAccountEntity walletAccountEntity = walletAccountService.findByWalletAndAccount(walletEntity, accountNumber);
        //find custom have some priority: (Level, currency)
        //
        return limitationGeneralCustomService.getSetting(channelEntity, limitationName, walletAccountEntity.getWalletEntity().getWalletLevelEntity(), walletAccountEntity.getWalletAccountTypeEntity(),
                walletAccountEntity.getWalletAccountCurrencyEntity(), walletTypeEntity);
    }

    @Override
    public LimitationListResponse getAll() throws InternalServiceException {
        List<LimitationGeneralEntity> limitationGeneralEntityList = limitationGeneralService.getLimitationGeneralEntities();
        return helper.fillLimitationListResponse(limitationGeneralEntityList);
    }
}
