package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletTypeRepositoryService {

    public static String NORMAL_USER = "NORMAL_USER";
    public static String CHANNEL = "CHANNEL";
    public static String MERCHANT = "MERCHANT";
    public static String COLLATERAL = "COLLATERAL";

    List<WalletTypeEntity> getAll();
    void clearCache();
    WalletTypeEntity getByName(String name);
    WalletTypeEntity getById(Long id) throws InternalServiceException;
    
    // Methods for getting managed entities from master database
    List<WalletTypeEntity> getAllManaged();
    WalletTypeEntity getByNameManaged(String name);
    WalletTypeEntity getByIdManaged(Long id) throws InternalServiceException;
}
