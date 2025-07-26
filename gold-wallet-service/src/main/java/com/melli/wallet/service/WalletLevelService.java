package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletLevelService {

    public static String BRONZE = "BRONZE";
    public static String SILVER = "SILVER";
    public static String GOLD = "GOLD";
    public static String PLATINUM = "PLATINUM";

    List<WalletLevelEntity> getAll();
    void clearCache();
    WalletLevelEntity getById(Long id) throws InternalServiceException;
    WalletLevelEntity getByLevel(String level) throws InternalServiceException;
}
