package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.CollateralRepository;
import com.melli.wallet.domain.master.persistence.CollateralWalletAccountCurrencyRepository;
import com.melli.wallet.domain.master.persistence.MerchantRepository;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.response.collateral.CollateralResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.CollateralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.service.repository.WalletAccountRepositoryService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: MerchantServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = ConstantRedisName.WALLET_COLLATERAL)
public class CollateralRepositoryServiceImplementation implements CollateralRepositoryService {

    private final CollateralRepository collateralRepository;
    private final CollateralWalletAccountCurrencyRepository collateralWalletAccountCurrencyRepository;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final Helper helper;
    private final WalletAccountRepositoryService walletAccountRepositoryService;



    @Override
    public CollateralResponse get(ChannelEntity channelEntity, String currency) throws InternalServiceException {
       WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(currency);
        List<CollateralWalletAccountCurrencyEntity> merchantWalletAccountCurrencyEntityList = collateralWalletAccountCurrencyRepository.findByWalletAccountCurrencyEntity(walletAccountCurrencyEntity);
        if(merchantWalletAccountCurrencyEntityList.isEmpty()){
            return new CollateralResponse();
        }
        return helper.fillCollateralResponse(merchantWalletAccountCurrencyEntityList.stream().map(CollateralWalletAccountCurrencyEntity::getCollateralEntity).toList());
    }



    @Override
    @Cacheable(unless = "#result == null")
    public CollateralEntity  findById(int id) {
        return collateralRepository.findById(id);
    }

    @Override
    public void save(CollateralEntity collateralEntity) {
        collateralRepository.save(collateralEntity);
    }

    @Override
    @CacheEvict
    public void clearAllCache() {
        log.info("start clear all merchant");
    }

    public CollateralEntity findCollateral(String collateralId) throws InternalServiceException {
        CollateralEntity collateral = collateralRepository.findById(Integer.parseInt(collateralId));
        if (collateral == null) {
            log.error("Collateral ID {} doesn't exist", collateralId);
            throw new InternalServiceException(
                    "Collateral doesn't exist",
                    StatusRepositoryService.COLLATERAL_NOT_FOUND,
                    HttpStatus.OK
            );
        }
        return collateral;
    }

    public WalletAccountEntity findCollateralWalletAccount(
            CollateralEntity collateralEntity, WalletAccountCurrencyEntity currencyEntity) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountRepositoryService.findByWallet(collateralEntity.getWalletEntity());
        if (CollectionUtils.isEmpty(accounts)) {
            log.error("No wallet accounts found for collateral {}", collateralEntity.getName());
            throw new InternalServiceException(
                    "Collateral wallet account not found",
                    StatusRepositoryService.MERCHANT_WALLET_ACCOUNT_NOT_FOUND,
                    HttpStatus.OK
            );
        }
        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getId() == (currencyEntity.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Wallet account with currency {} not found for merchant {}", currencyEntity.getName(), collateralEntity.getName());
                    return new InternalServiceException(
                            "Wallet account not found for merchant",
                            StatusRepositoryService.MERCHANT_WALLET_ACCOUNT_NOT_FOUND,
                            HttpStatus.OK
                    );
                });
    }

}
