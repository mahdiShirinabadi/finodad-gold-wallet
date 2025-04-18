package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import com.melli.wallet.domain.master.persistence.WalletLevelRepository;
import com.melli.wallet.service.WalletLevelService;
import com.melli.wallet.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: WalletLevelServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = Constant.WALLET_LEVEL_CACHE)
public class WalletLevelServiceImplementation implements WalletLevelService {

    private final WalletLevelRepository walletLevelRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletLevelEntity> getAll() {
        return walletLevelRepository.findAll();
    }

    @Override
    public void clearCache() {
        log.info("clear cache ({})", Constant.WALLET_LEVEL_CACHE);
    }
}
