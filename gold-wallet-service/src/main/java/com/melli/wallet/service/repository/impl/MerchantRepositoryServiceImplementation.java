package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.MerchantRepository;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@CacheConfig(cacheNames = ConstantRedisName.WALLET_MERCHANT)
public class MerchantRepositoryServiceImplementation implements MerchantRepositoryService {

    private final MerchantRepository merchantRepository;
    private final MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final Helper helper;


    @Override
    public MerchantWalletAccountCurrencyEntity checkPermissionOnCurrency(WalletAccountCurrencyEntity walletAccountCurrencyEntity, MerchantEntity merchant) throws InternalServiceException {

        List<MerchantWalletAccountCurrencyEntity> merchantWalletAccountCurrencyEntityList = merchantWalletAccountCurrencyRepository.findByWalletAccountCurrencyEntityAndMerchantEntity(walletAccountCurrencyEntity, merchant);
        if(merchantWalletAccountCurrencyEntityList.isEmpty()){
            log.info("merchant ({}) on currency ({}) dont have a permission", merchant.getName(), walletAccountCurrencyEntity.getName());
            throw new InternalServiceException("merchant not found on currency", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        return merchantWalletAccountCurrencyEntityList.getFirst();
    }

    @Override
    public MerchantResponse getMerchant(ChannelEntity channelEntity, String currency) throws InternalServiceException {
       WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccountCurrencyRepositoryService.findCurrency(currency);
        List<MerchantWalletAccountCurrencyEntity> merchantWalletAccountCurrencyEntityList = merchantWalletAccountCurrencyRepository.findByWalletAccountCurrencyEntity(walletAccountCurrencyEntity);
        if(merchantWalletAccountCurrencyEntityList.isEmpty()){
            return new MerchantResponse();
        }
        return helper.fillMerchantResponse(merchantWalletAccountCurrencyEntityList.stream().map(MerchantWalletAccountCurrencyEntity::getMerchantEntity).toList());
    }



    @Override
    @Cacheable(unless = "#result == null")
    public MerchantEntity  findById(int id) {
        return merchantRepository.findById(id);
    }

    @Override
    public void save(MerchantEntity merchant) {
        merchantRepository.save(merchant);
    }

    @Override
    @CacheEvict
    public void clearAllCache() {
        log.info("start clear all merchant");
    }

}
