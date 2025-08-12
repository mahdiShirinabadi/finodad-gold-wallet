package com.melli.wallet.service.repository;

import com.melli.wallet.domain.redis.WalletDailyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;

/**
 * Class Name: WalletSellLimitationRepositoryService
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
public interface WalletBuyLimitationRepositoryService {

    void deleteAll();
    void saveMonthly(WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis);
    void saveDaily(WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis);
    WalletDailyBuyLimitationRedis findDailyById(String id);
    WalletMonthlyBuyLimitationRedis findMonthlyById(String id);
}
