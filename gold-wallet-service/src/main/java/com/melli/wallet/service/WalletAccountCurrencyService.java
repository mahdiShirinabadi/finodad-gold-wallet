package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.exception.InternalServiceException;

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
    WalletAccountCurrencyEntity findCurrency(String currency) throws InternalServiceException;
}
