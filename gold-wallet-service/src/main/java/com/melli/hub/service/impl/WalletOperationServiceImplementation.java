package com.melli.hub.service.impl;

import com.melli.hub.domain.enumaration.WalletStatusEnum;
import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.master.persistence.WalletLevelRepository;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.wallet.CreateWalletResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.util.StringUtils;
import com.melli.hub.util.Validator;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Class Name: WalletOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletOperationServiceImplementation implements WalletOperationalService {

    private final ShahkarInfoOperationService shahkarInfoOperationService;
    private final RedisLockService redisLockService;
    private final WalletService walletService;
    private final Helper helper;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final WalletLevelService walletLevelService;

    @Override
    public CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletTypeString, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException {
        if (channelEntity.getCheckShahkar() == ChannelService.TRUE || StringUtils.hasText(nationalCode)) {
            log.info("Start checking if nationalCode({}) and mobile({}) are related from shahkar or not ...", nationalCode, mobile);
            shahkarInfoOperationService.checkShahkarInfo(mobile, nationalCode, false);
            log.info("nationalCode({}) and mobile({}) are related together.", nationalCode, mobile);
        }

        Optional<WalletTypeEntity> walletTypeEntity = walletTypeService.getAll().stream().filter(x -> x.getName().equals(walletTypeString)).findFirst();
        if (walletTypeEntity.isEmpty()) {
            log.error("wallet type with name ({}) not found", walletTypeString);
            throw new InternalServiceException("walletType not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return redisLockService.runAfterLock(nationalCode, this.getClass(), () -> {

            WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.get().getId());
            if (walletEntity != null) {
                List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletEntity);
                log.info("Wallet already exists with id {}", walletEntity.getId());
                if (walletAccountEntityList.isEmpty()) {
                    log.error("walletAccount is not create success");
                    throw new InternalServiceException("walletAccount is not create success", StatusService.WALLET_NOT_CREATE_SUCCESS, HttpStatus.OK);
                }
                return helper.fillCreateWalletResponse(walletEntity, walletAccountEntityList, walletAccountService);
            }

            log.info("start create wallet with nationalCode ===> {}", nationalCode);

            walletEntity = new WalletEntity();
            walletEntity.setMobile(mobile);
            walletEntity.setNationalCode(nationalCode);
            walletEntity.setDescription("");
            walletEntity.setOwner(channelEntity);
            walletEntity.setWalletTypeEntity(walletTypeEntity.get());
            walletEntity.setStatus(WalletStatusEnum.ACTIVE);
            walletEntity.setWalletLEvelEntity(walletLevelService.getAll().stream().filter(x -> x.getName().equals(WalletLevelService.ONE)).findFirst().get());
            walletEntity.setCreatedBy(nationalCode);
            walletEntity.setCreatedAt(new Date());
            walletService.save(walletEntity);

            walletAccountService.createAccount(walletAccountCurrencyList, walletEntity, walletAccountTypeList, channelEntity);

            return helper.fillCreateWalletResponse(walletEntity, walletAccountService.findByWallet(walletEntity), walletAccountService);
        }, nationalCode);
    }

    @Override
    public BaseResponse deactivateWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletService.findById(Long.parseLong(walletId));
        walletEntity.setStatus(WalletStatusEnum.DISABLE);
        walletEntity.setUpdatedAt(new Date());
        walletService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public BaseResponse deleteWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletService.findById(Long.parseLong(walletId));
        walletEntity.setNationalCode(walletEntity.getMobile() + "-" + walletEntity.getId());
        walletEntity.setStatus(WalletStatusEnum.DELETED);
        walletEntity.setEndTime(new Date());
        walletService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public BaseResponse activateWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletService.findById(Long.parseLong(walletId));
        walletEntity.setStatus(WalletStatusEnum.ACTIVE);
        walletEntity.setUpdatedAt(new Date());
        walletService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public CreateWalletResponse get(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException {

        Optional<WalletTypeEntity> walletTypeEntity = walletTypeService.getAll().stream().filter(x -> x.getName().equals(WalletTypeService.NORMAL_USER)).findFirst();
        if (walletTypeEntity.isEmpty()) {
            log.error("walletType with name ({}) not found", WalletTypeService.NORMAL_USER);
            throw new InternalServiceException("walletType not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.get().getId());
        if (walletEntity == null) {
            log.error("wallet is not create");
            throw new InternalServiceException("walletAccount is not create success", StatusService.WALLET_NOT_FOUND, HttpStatus.OK);
        }

        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletEntity);
        if (walletAccountEntityList.isEmpty()) {
            log.error("walletAccount is not create success");
            throw new InternalServiceException("walletAccount is not create success", StatusService.WALLET_NOT_CREATE_SUCCESS, HttpStatus.OK);
        }
        return helper.fillCreateWalletResponse(walletEntity, walletAccountEntityList, walletAccountService);
    }
}
