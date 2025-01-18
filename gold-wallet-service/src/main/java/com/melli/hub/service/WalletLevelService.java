package com.melli.hub.service;

import com.melli.hub.domain.master.entity.WalletLevelEntity;
import com.melli.hub.domain.master.entity.WalletTypeEntity;

import java.util.List;

/**
 * Class Name: WalletAccountCurrencyService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletLevelService {

    public static String ONE = "ONE";
    public static String TWO = "TWO";
    public static String THREE = "THREE";

    List<WalletLevelEntity> getAll();
    void clearCache();
}
