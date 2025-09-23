package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.persistence.WalletGiftCardLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletGiftCardPaymentLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailyPaymentGiftCardLimitationRedis;
import com.melli.wallet.service.repository.WalletGiftCardPaymentLimitationRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: WalletGiftCardPaymentLimitationRepositoryServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/23/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletGiftCardPaymentLimitationRepositoryServiceImplementation implements WalletGiftCardPaymentLimitationRepositoryService {


    private final WalletGiftCardPaymentLimitationRepository walletGiftCardPaymentLimitationRepository;

    @Override
    public void deleteAll() {
        walletGiftCardPaymentLimitationRepository.deleteAll();
    }

    @Override
    public void saveDaily(WalletDailyPaymentGiftCardLimitationRedis walletDailyPaymentGiftCardLimitationRedis) {

        walletGiftCardPaymentLimitationRepository.save(walletDailyPaymentGiftCardLimitationRedis);
    }

    @Override
    public WalletDailyPaymentGiftCardLimitationRedis findDailyById(String id) {
        return walletGiftCardPaymentLimitationRepository.findById(id).orElse(null);
    }
}
