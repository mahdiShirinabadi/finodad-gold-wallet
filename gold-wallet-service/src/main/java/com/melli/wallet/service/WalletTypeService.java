package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletTypeService {

    public static String NORMAL_USER = "NORMAL_USER";
    public static String CHANNEL = "CHANNEL";
    public static String MERCHANT = "MERCHANT";

    List<WalletTypeEntity> getAll();
    void clearCache();
    WalletTypeEntity getByName(String name);
    WalletTypeEntity getById(Long id) throws InternalServiceException;
}
