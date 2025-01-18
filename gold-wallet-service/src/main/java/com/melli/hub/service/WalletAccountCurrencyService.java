package com.melli.hub.service;

import com.melli.hub.domain.master.entity.WalletAccountCurrencyEntity;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletAccountCurrencyService {

    public static String RIAL = "RIAL";
    public static String GOLD = "GOLD";

    List<WalletAccountCurrencyEntity> getAll();
    void clearCache();
}
