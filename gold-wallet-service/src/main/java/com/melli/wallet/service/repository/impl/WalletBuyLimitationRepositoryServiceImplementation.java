package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.persistence.WalletDailyBuyLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletDailySellLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletMonthlyBuyLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletMonthlySellLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletDailySellLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;
import com.melli.wallet.service.repository.WalletBuyLimitationRepositoryService;
import com.melli.wallet.service.repository.WalletSellLimitationRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: WalletSellLimitationRepositoryServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletBuyLimitationRepositoryServiceImplementation implements WalletBuyLimitationRepositoryService {

    private final WalletMonthlyBuyLimitationRepository walletMonthlyBuyLimitationRepository;
    private final WalletDailyBuyLimitationRepository walletDailyBuyLimitationRepository;


    @Override
    public void deleteAll() {
        log.info("delete all buy daily");
        walletMonthlyBuyLimitationRepository.deleteAll();
        log.info("delete all buy monthly");
        walletDailyBuyLimitationRepository.deleteAll();
    }

    public void saveDaily(WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis) {
        log.info("save WalletDailyBuyLimitationRedis with data ({})", walletDailyBuyLimitationRedis.toString());
        walletDailyBuyLimitationRepository.save(walletDailyBuyLimitationRedis);
        log.info("success WalletDailyBuyLimitationRedis with data ({})", walletDailyBuyLimitationRedis.toString());
    }

    public void saveMonthly(WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis) {
        walletMonthlyBuyLimitationRepository.save(walletMonthlyBuyLimitationRedis);
    }

    public WalletDailyBuyLimitationRedis findDailyById(String id) {
        return walletDailyBuyLimitationRepository.findById(id).orElse(null);
    }

    public WalletMonthlyBuyLimitationRedis findMonthlyById(String id) {
        return walletMonthlyBuyLimitationRepository.findById(id).orElse(null);
    }
}
