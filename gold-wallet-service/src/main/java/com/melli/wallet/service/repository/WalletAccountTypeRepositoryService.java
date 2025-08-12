package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletAccountTypeRepositoryService {

    public static String NORMAL = "NORMAL";
    public static String WAGE = "WAGE";

    List<WalletAccountTypeEntity> getAll();
    void clearCache();
    WalletAccountTypeEntity findByName(String name);
    WalletAccountTypeEntity getById(Long id) throws InternalServiceException;
    
    // Methods for getting managed entities from master database
    List<WalletAccountTypeEntity> getAllManaged();
    WalletAccountTypeEntity findByNameManaged(String name);
    WalletAccountTypeEntity getByIdManaged(Long id) throws InternalServiceException;
}
