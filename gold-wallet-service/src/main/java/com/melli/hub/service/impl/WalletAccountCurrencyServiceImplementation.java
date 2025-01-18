package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.hub.domain.master.persistence.WalletAccountCurrencyRepository;
import com.melli.hub.service.WalletAccountCurrencyService;
import com.melli.hub.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = Constant.WALLET_ACCOUNT_CURRENCY_CACHE)
public class WalletAccountCurrencyServiceImplementation implements WalletAccountCurrencyService {


    private final WalletAccountCurrencyRepository walletAccountCurrencyRepository;


    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletAccountCurrencyEntity> getAll() {
        return walletAccountCurrencyRepository.findAll();
    }

    @Override
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("clear cache ({})", Constant.WALLET_ACCOUNT_CURRENCY_CACHE);
    }
}
