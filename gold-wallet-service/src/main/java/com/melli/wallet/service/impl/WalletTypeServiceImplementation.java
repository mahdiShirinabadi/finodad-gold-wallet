package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.master.persistence.WalletTypeRepository;
import com.melli.wallet.service.WalletTypeService;
import com.melli.wallet.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: WalletTypeServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = Constant.WALLET_TYPE_CACHE)
public class WalletTypeServiceImplementation implements WalletTypeService {

    private final WalletTypeRepository walletTypeRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletTypeEntity> getAll() {
        return walletTypeRepository.findAll();
    }

    @Override
    public void clearCache() {
        log.info("clear cache ({})", Constant.WALLET_TYPE_CACHE);
    }
}
