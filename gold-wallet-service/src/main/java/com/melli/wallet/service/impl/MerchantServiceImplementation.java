package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.persistence.MerchantRepository;
import com.melli.wallet.service.MerchantService;
import com.melli.wallet.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class Name: MerchantServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = Constant.WALLET_MERCHANT)
public class MerchantServiceImplementation implements MerchantService {

    private final MerchantRepository merchantRepository;

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
