package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.WalletAccountCurrencyRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletAccountCurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
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
@CacheConfig(cacheNames = com.melli.wallet.ConstantRedisName.WALLET_ACCOUNT_CURRENCY_CACHE)
public class WalletAccountCurrencyServiceImplementation implements WalletAccountCurrencyService {


    private final WalletAccountCurrencyRepository walletAccountCurrencyRepository;


    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletAccountCurrencyEntity> getAll() {
        return walletAccountCurrencyRepository.findAll();
    }

    @Cacheable(key = "#currency.toLowerCase()", unless = "#result == null")
    public WalletAccountCurrencyEntity findCurrency(String currency) throws InternalServiceException {
        return walletAccountCurrencyRepository.findByNameIgnoreCase(currency).orElseThrow(() -> {
            log.error("Currency {} not supported", currency);
            return new InternalServiceException("Currency not supported", StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, HttpStatus.OK);
        });
    }


    @Override
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("clear cache ({})", ConstantRedisName.WALLET_ACCOUNT_CURRENCY_CACHE);
    }

    @Override
    public WalletAccountCurrencyEntity getById(Long id) throws InternalServiceException {
        return walletAccountCurrencyRepository.findById(id).orElseThrow(() -> {
            log.error("wallet account currency with id ({}) not found", id);
            return new InternalServiceException("Wallet account currency not found",  StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, HttpStatus.OK);
        });
    }
}
