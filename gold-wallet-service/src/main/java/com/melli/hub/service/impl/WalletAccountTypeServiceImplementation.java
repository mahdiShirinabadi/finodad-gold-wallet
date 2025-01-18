package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.WalletAccountTypeEntity;
import com.melli.hub.domain.master.persistence.WalletAccountTypeRepository;
import com.melli.hub.service.WalletAccountTypeService;
import com.melli.hub.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: WalletAccountTypeServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = Constant.WALLET_ACCOUNT_TYPE_CACHE)
public class WalletAccountTypeServiceImplementation implements WalletAccountTypeService {

    private final WalletAccountTypeRepository walletAccountTypeRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletAccountTypeEntity> getAll() {
        return walletAccountTypeRepository.findAll();
    }

    @Override
    public void clearCache() {
        log.info("clear cache ({})", Constant.WALLET_ACCOUNT_TYPE_CACHE);
    }
}
