package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.master.persistence.WalletTypeRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
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
@CacheConfig(cacheNames = com.melli.wallet.ConstantRedisName.WALLET_TYPE_CACHE)
public class WalletTypeServiceImplementation implements WalletTypeService {

    private final WalletTypeRepository walletTypeRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletTypeEntity> getAll() {
        return walletTypeRepository.findAll();
    }

    @Override
    public void clearCache() {
        log.info("clear cache ({})", ConstantRedisName.WALLET_TYPE_CACHE);
    }

    @Override
    @Cacheable(key = "{#name}", unless = "#result == null")
    public WalletTypeEntity getByName(String name) {
        return walletTypeRepository.findByName(name);
    }

    @Override
    public WalletTypeEntity getById(Long id) throws InternalServiceException {
        return walletTypeRepository.findById(id).orElseThrow(() -> {
            log.error("wallet type with id ({}) not found", id);
            return new InternalServiceException("Wallet type not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        });
    }
}
