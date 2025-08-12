package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import com.melli.wallet.domain.master.persistence.WalletLevelRepository;
import com.melli.wallet.domain.slave.entity.ReportWalletLevelEntity;
import com.melli.wallet.domain.slave.persistence.ReportWalletLevelRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.mapper.WalletLevelMapper;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
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
@CacheConfig(cacheNames = com.melli.wallet.ConstantRedisName.WALLET_LEVEL_CACHE)
public class WalletLevelServiceImplementation implements WalletLevelService {

    private final WalletLevelRepository walletLevelRepository;
    private final ReportWalletLevelRepository reportWalletLevelRepository;
    private final WalletLevelMapper walletLevelMapper;

    @Override
    @Cacheable(unless = "#result == null")
    public List<WalletLevelEntity> getAll() {
        // For read operations, use slave database
        List<ReportWalletLevelEntity> reportEntities = reportWalletLevelRepository.findAll();
        return walletLevelMapper.toWalletLevelEntityList(reportEntities);
    }

    /**
     * Get managed entities from master database for operations that need to save relationships
     */
    public List<WalletLevelEntity> getAllManaged() {
        return walletLevelRepository.findAll();
    }

    @Override
    public void clearCache() {
        log.info("clear cache ({})", ConstantRedisName.WALLET_LEVEL_CACHE);
    }

    @Override
    public WalletLevelEntity getById(Long id) throws InternalServiceException {
        ReportWalletLevelEntity reportEntity = reportWalletLevelRepository.findById(id).orElse(null);
        if (reportEntity == null) {
            log.error("wallet level with id ({}) not found", id);
            throw new InternalServiceException("Wallet level not found", StatusService.WALLET_LEVEL_NOT_FOUND, HttpStatus.OK);
        }
        return walletLevelMapper.toWalletLevelEntity(reportEntity);
    }

    /**
     * Get managed entity from master database for operations that need to save relationships
     */
    public WalletLevelEntity getByIdManaged(Long id) throws InternalServiceException {
        return walletLevelRepository.findById(id).orElseThrow(() -> {
            log.error("wallet level with id ({}) not found", id);
            return new InternalServiceException("Wallet level not found", StatusService.WALLET_LEVEL_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public WalletLevelEntity getByLevel(String level) throws InternalServiceException {
        ReportWalletLevelEntity reportEntity = reportWalletLevelRepository.getByName(level);
        if (reportEntity == null) {
            log.error("wallet level with name ({}) not found", level);
            throw new InternalServiceException("Wallet level not found", StatusService.WALLET_LEVEL_NOT_FOUND, HttpStatus.OK);
        }
        return walletLevelMapper.toWalletLevelEntity(reportEntity);
    }

    /**
     * Get managed entity from master database for operations that need to save relationships
     */
    public WalletLevelEntity getByLevelManaged(String level) throws InternalServiceException {
        WalletLevelEntity entity = walletLevelRepository.getByName(level);
        if (entity == null) {
            log.error("wallet level with name ({}) not found", level);
            throw new InternalServiceException("Wallet level not found", StatusService.WALLET_LEVEL_NOT_FOUND, HttpStatus.OK);
        }
        return entity;
    }
}
