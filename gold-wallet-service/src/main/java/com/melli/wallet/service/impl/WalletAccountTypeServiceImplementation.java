package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;
import com.melli.wallet.domain.master.persistence.WalletAccountTypeRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletAccountTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
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
@CacheConfig(cacheNames = com.melli.wallet.ConstantRedisName.WALLET_ACCOUNT_TYPE_CACHE)
public class WalletAccountTypeServiceImplementation implements WalletAccountTypeService {

    private final WalletAccountTypeRepository walletAccountTypeRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletAccountTypeEntity> getAll() {
        return walletAccountTypeRepository.findAll();
    }

    @Override
    @Cacheable(key = "{#name}", unless = "#result == null")
    public WalletAccountTypeEntity findByName(String name) {
        log.info("Starting retrieval of Status by code: {}", name);
        return walletAccountTypeRepository.findByName(name);
    }

    @Override
    public void clearCache() {
        log.info("clear cache ({})", ConstantRedisName.WALLET_ACCOUNT_TYPE_CACHE);
    }

    @Override
    public WalletAccountTypeEntity getById(Long id) throws InternalServiceException {
        return walletAccountTypeRepository.findById(id).orElseThrow(() -> {
            log.error("wallet account type with id ({}) not found", id);
            return new InternalServiceException("Wallet account type not found", StatusService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
        });
    }
}
