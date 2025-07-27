package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.master.persistence.WalletRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@CacheConfig(cacheNames = ConstantRedisName.WALLET_NAME_CACHE)
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletServiceImplementation implements WalletService {


    private final WalletRepository walletRepository;

    @Override
    @Cacheable(key = "{#nationalCode, #walletTypeEntityId}",unless = "#result == null")
    public WalletEntity findByNationalCodeAndWalletTypeId(String nationalCode, long walletTypeEntityId) {
        log.info("start find wallet with nationalCode ===> ({})", nationalCode);
        WalletEntity wallet = walletRepository.findByNationalCodeAndWalletTypeEntityIdAndEndTimeIsNull(nationalCode, walletTypeEntityId);
        log.info("finish find wallet with nationalCode ===> ({})", nationalCode);
        return wallet;
    }


    @Override
    public WalletEntity findById(Long walletId) throws InternalServiceException {
        log.info("start find wallet with walletId ===> ({})", walletId);
        WalletEntity wallet = walletRepository.findById(walletId).orElseThrow(()->{
            log.error("wallet with id ({}) not found", walletId);
            return new InternalServiceException("wallet not found", StatusService.WALLET_NOT_FOUND, HttpStatus.OK);
        });
        log.info("finish find wallet with walletId ===> ({})", walletId);
        return wallet;
    }


    @Override
    @CacheEvict(key = "{#walletEntity?.nationalCode, #walletEntity?.walletTypeEntity?.id}")
    public void save(WalletEntity walletEntity) {
        log.info("start save wallet with info ===> ({})", walletEntity.getNationalCode());
        walletRepository.save(walletEntity);
        log.info("finish save wallet with info ===> ({})", walletEntity.getNationalCode());
    }


    @Override
    @CacheEvict(allEntries = true)
    public void clearAllCache() {
        log.info("start delete all wallet");
    }

    @Override
    public List<WalletEntity> findAllByStatus(int status) {
        return walletRepository.findAllByStatus(status);
    }

}
