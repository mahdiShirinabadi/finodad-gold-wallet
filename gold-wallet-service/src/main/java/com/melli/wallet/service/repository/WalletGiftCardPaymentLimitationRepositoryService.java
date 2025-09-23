package com.melli.wallet.service.repository;

import com.melli.wallet.domain.redis.WalletDailyGiftCardLimitationRedis;
import com.melli.wallet.domain.redis.WalletDailyPaymentGiftCardLimitationRedis;

/**
 * Class Name: WalletSellLimitationRepositoryService
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
public interface WalletGiftCardPaymentLimitationRepositoryService {

    void deleteAll();
    void saveDaily(WalletDailyPaymentGiftCardLimitationRedis walletDailyPaymentGiftCardLimitationRedis);
    WalletDailyPaymentGiftCardLimitationRedis findDailyById(String id);
}
