package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.master.persistence.WalletRepository;
import com.melli.wallet.domain.slave.entity.ReportWalletEntity;
import com.melli.wallet.domain.slave.persistence.ReportWalletRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@CacheConfig(cacheNames = ConstantRedisName.WALLET_NAME_CACHE)
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletRepositoryServiceImplementation implements WalletRepositoryService {


    private final WalletRepository walletRepository;
    private final ReportWalletRepository reportWalletRepository;

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
            return new InternalServiceException("wallet not found", StatusRepositoryService.WALLET_NOT_FOUND, HttpStatus.OK);
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
    public List<WalletEntity> findAllByStatus(String status) {
        return walletRepository.findAllByStatus(WalletStatusEnum.valueOf(status));
    }

    @Override
    public List<WalletEntity> findWalletsWithFilters(String status, String nationalCode, String mobile) {
        log.info("start find wallets with filters - status: {}, nationalCode: {}, mobile: {}", status, nationalCode, mobile);
        WalletStatusEnum statusEnum = WalletStatusEnum.valueOf(status);
        List<WalletEntity> wallets = walletRepository.findByStatusAndNationalCodeContainingAndMobileContaining(statusEnum, nationalCode, mobile);
        log.info("found {} wallets with filters", wallets.size());
        return wallets;
    }

    @Override
    public Page<ReportWalletEntity> findWalletsWithFiltersAndPagination(String status, String nationalCode, String mobile,
                                                                        String fromTime, String toTime, Pageable pageable) {
        log.info("start find wallets with filters and pagination - status: {}, nationalCode: {}, mobile: {}, fromTime: {}, toTime: {}", 
                status, nationalCode, mobile, fromTime, toTime);
        
        String fromTimeStr = null;
        if (CustomStringUtils.hasText(fromTime)) {
            Date sDate;
            if (Integer.parseInt(fromTime.substring(0, 4)) < 1900) {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            if (sDate != null) {
                fromTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sDate);
            }
        }

        String toTimeStr = null;
        if (CustomStringUtils.hasText(toTime)) {
            Date tDate;
            if (Integer.parseInt(toTime.substring(0, 4)) < 1900) {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            if (tDate != null) {
                toTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tDate);
            }
        }

        Page<ReportWalletEntity> wallets = reportWalletRepository.findWalletsWithFilters(nationalCode, mobile, fromTimeStr, toTimeStr, pageable);
        log.info("found {} wallets with filters and pagination", wallets.getTotalElements());
        return wallets;
    }

}
