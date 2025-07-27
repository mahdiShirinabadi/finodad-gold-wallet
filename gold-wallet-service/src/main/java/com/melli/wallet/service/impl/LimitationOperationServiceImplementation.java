package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.limitation.LimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private final WalletLevelService walletLevelService;
    private final WalletAccountTypeService walletAccountTypeService;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final Helper helper;
    private final RedisLockService redisLockService;
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

    @Override
    public void updateLimitationGeneral(Long id, String value, String pattern, ChannelEntity channelEntity) throws InternalServiceException {
        log.info("start update limitation general with id ({}) by user ({}) - value: {}, pattern: {}",
                id, channelEntity.getUsername(), value, pattern);

        LimitationGeneralEntity limitationGeneralEntity = limitationGeneralService.getById(id);
        if(limitationGeneralEntity == null) {
            log.error("limitationGeneralEntity with id ({}) not found", id);
            throw new InternalServiceException("General limitation with this name not found", StatusService.LIMITATION_NOT_FOUND, HttpStatus.OK);
        }
        limitationGeneralEntity.setValue(value);
        limitationGeneralEntity.setPattern(pattern);
        limitationGeneralEntity.setUpdatedAt(new Date());
        limitationGeneralEntity.setUpdatedBy(channelEntity.getUsername());
        redisLockService.runAfterLock(String.valueOf(id), this.getClass(),()->{
            limitationGeneralService.save(limitationGeneralEntity);
            log.info("limitationGeneralEntity with name ({}) updated successfully - value: {}, pattern: {}", limitationGeneralEntity.getName(), limitationGeneralEntity.getValue(), limitationGeneralEntity.getPattern());
            return null;
        },String.valueOf(id));
        log.info("limitation general with name ({}) updated successfully - value: {}, pattern: {}",
                id, value, pattern);
        limitationGeneralService.clearCache();
    }

    @Override
    public void insertLimitationGeneralCustom(Long limitationGeneralId, String value, String additionalData, Long walletLevelId, Long walletAccountTypeId, Long walletAccountCurrencyId, Long walletTypeId, Long channelId, ChannelEntity channelEntity) throws InternalServiceException {
        log.info("start insert limitation general custom with ID ({}) by user ({})", limitationGeneralId, channelEntity.getUsername());
        
        // Get required entities
        WalletLevelEntity walletLevelEntity = null;
        WalletAccountTypeEntity walletAccountTypeEntity = null;
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = null;
        WalletTypeEntity walletTypeEntity = null;
        
        if (walletLevelId != null) {
            walletLevelEntity = walletLevelService.getById(walletLevelId);
        }
        
        if (walletAccountTypeId != null) {
            walletAccountTypeEntity = walletAccountTypeService.getById(walletAccountTypeId);
        }
        
        if (walletAccountCurrencyId != null) {
            walletAccountCurrencyEntity = walletAccountCurrencyService.getById(walletAccountCurrencyId);
        }
        
        if (walletTypeId != null) {
            walletTypeEntity = walletTypeService.getById(walletTypeId);
        }
        
        limitationGeneralCustomService.create(
            channelEntity,
            limitationGeneralId,
            walletLevelEntity,
            walletAccountTypeEntity,
            walletAccountCurrencyEntity,
            walletTypeEntity,
            value,
            additionalData
        );
        
        log.info("limitation general custom with ID ({}) inserted successfully", limitationGeneralId);
    }
}
