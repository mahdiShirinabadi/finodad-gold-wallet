package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.persistence.WalletDailyP2PLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletGiftCardLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailyGiftCardLimitationRedis;
import com.melli.wallet.domain.redis.WalletDailyP2pLimitationRedis;
import com.melli.wallet.service.repository.WalletGiftCardLimitationRepositoryService;
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
public class WalletGiftCardLimitationRepositoryServiceImplementation implements WalletGiftCardLimitationRepositoryService {

    private final WalletGiftCardLimitationRepository walletGiftCardLimitationRepository;


    @Override
    public void deleteAll() {
        log.info("delete all giftCard daily");
        walletGiftCardLimitationRepository.deleteAll();
    }

    public void saveDaily(WalletDailyGiftCardLimitationRedis walletDailyGiftCardLimitationRedis) {
        walletGiftCardLimitationRepository.save(walletDailyGiftCardLimitationRedis);
    }

    public WalletDailyGiftCardLimitationRedis findDailyById(String id) {
        return walletGiftCardLimitationRepository.findById(id).orElse(null);
    }

}
