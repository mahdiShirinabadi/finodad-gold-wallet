package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.persistence.WalletDailySellLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletMonthlySellLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailySellLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;
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
public class WalletSellLimitationRepositoryServiceImplementation implements WalletSellLimitationRepositoryService {

    private final WalletMonthlySellLimitationRepository walletMonthlySellLimitationRepository;
    private final WalletDailySellLimitationRepository walletDailySellLimitationRepository;


    @Override
    public void deleteAll() {
        log.info("delete all sell daily");
        walletMonthlySellLimitationRepository.deleteAll();
        log.info("delete all sell monthly");
        walletDailySellLimitationRepository.deleteAll();
    }

    public void saveDaily(WalletDailySellLimitationRedis walletDailySellLimitationRedis) {
        walletDailySellLimitationRepository.save(walletDailySellLimitationRedis);
    }

    public void saveMonthly(WalletMonthlySellLimitationRedis walletMonthlySellLimitationRedis) {
        walletMonthlySellLimitationRepository.save(walletMonthlySellLimitationRedis);
    }

    public WalletDailySellLimitationRedis findDailyById(String id) {
        return walletDailySellLimitationRepository.findById(id).orElse(null);
    }

    public WalletMonthlySellLimitationRedis findMonthlyById(String id) {
        return walletMonthlySellLimitationRepository.findById(id).orElse(null);
    }
}
