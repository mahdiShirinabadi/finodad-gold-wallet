package com.melli.wallet.service.repository;

import com.melli.wallet.domain.redis.WalletDailyP2pLimitationRedis;
import com.melli.wallet.domain.redis.WalletDailySellLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;

/**
 * Class Name: WalletSellLimitationRepositoryService
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
public interface WalletP2pLimitationRepositoryService {

    void deleteAll();
    void saveDaily(WalletDailyP2pLimitationRedis walletDailyP2pLimitationRedis);
    WalletDailyP2pLimitationRedis findDailyById(String id);
}
