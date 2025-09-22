package com.melli.wallet.service.repository;

import com.melli.wallet.domain.redis.WalletDailyGiftCardLimitationRedis;
import com.melli.wallet.domain.redis.WalletDailyP2pLimitationRedis;

/**
 * Class Name: WalletSellLimitationRepositoryService
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
public interface WalletGiftCardLimitationRepositoryService {

    void deleteAll();
    void saveDaily(WalletDailyGiftCardLimitationRedis walletDailyGiftCardLimitationRedis);
    WalletDailyGiftCardLimitationRedis findDailyById(String id);
}
