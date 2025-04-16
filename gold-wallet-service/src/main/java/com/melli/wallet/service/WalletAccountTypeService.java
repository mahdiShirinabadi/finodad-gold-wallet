package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletAccountTypeService {

    public static String NORMAL = "NORMAL";
    public static String WAGE = "WAGE";

    List<WalletAccountTypeEntity> getAll();
    void clearCache();
}
