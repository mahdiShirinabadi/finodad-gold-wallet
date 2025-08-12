package com.melli.wallet.service.repository;

import com.melli.wallet.domain.redis.WalletDailySellLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;

/**
 * Class Name: WalletSellLimitationRepositoryService
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
public interface WalletSellLimitationRepositoryService {

    void deleteAll();
    void saveDaily(WalletDailySellLimitationRedis walletDailySellLimitationRedis);
    void saveMonthly(WalletMonthlySellLimitationRedis walletMonthlySellLimitationRedis);
    WalletDailySellLimitationRedis findDailyById(String id);
    WalletMonthlySellLimitationRedis findMonthlyById(String id);
}
