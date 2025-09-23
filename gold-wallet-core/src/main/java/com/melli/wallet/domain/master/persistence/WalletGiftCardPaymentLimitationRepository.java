package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletDailyPaymentGiftCardLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletGiftCardPaymentLimitationRepository extends CrudRepository<WalletDailyPaymentGiftCardLimitationRedis, String> {

}
