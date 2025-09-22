package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletDailyGiftCardLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletGiftCardLimitationRepository extends CrudRepository<WalletDailyGiftCardLimitationRedis, String> {

}
