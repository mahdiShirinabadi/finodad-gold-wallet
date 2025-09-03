package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.persistence.WalletDailyP2PLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletDailySellLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailyP2pLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;
import com.melli.wallet.service.repository.WalletP2pLimitationRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: WalletP2pLimitationRepositoryServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletP2pLimitationRepositoryServiceImplementation implements WalletP2pLimitationRepositoryService {

    private final WalletDailyP2PLimitationRepository walletDailyP2PLimitationRepository;


    @Override
    public void deleteAll() {
        log.info("delete all sell daily");
        walletDailyP2PLimitationRepository.deleteAll();
    }

    public void saveDaily(WalletDailyP2pLimitationRedis walletDailySellLimitationRedis) {
        walletDailyP2PLimitationRepository.save(walletDailySellLimitationRedis);
    }

    public WalletDailyP2pLimitationRedis findDailyById(String id) {
        return walletDailyP2PLimitationRepository.findById(id).orElse(null);
    }

}
