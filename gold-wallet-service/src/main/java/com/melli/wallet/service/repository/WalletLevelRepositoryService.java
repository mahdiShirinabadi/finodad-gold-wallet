package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletLevelRepositoryService {

    public static String BRONZE = "BRONZE";
    public static String SILVER = "SILVER";
    public static String GOLD = "GOLD";
    public static String PLATINUM = "PLATINUM";

    List<WalletLevelEntity> getAll();
    void clearCache();
    WalletLevelEntity getById(Long id) throws InternalServiceException;
    WalletLevelEntity getByLevel(String level) throws InternalServiceException;
    
    // Methods for getting managed entities from master database
    List<WalletLevelEntity> getAllManaged();
    WalletLevelEntity getByIdManaged(Long id) throws InternalServiceException;
    WalletLevelEntity getByLevelManaged(String level) throws InternalServiceException;
}
