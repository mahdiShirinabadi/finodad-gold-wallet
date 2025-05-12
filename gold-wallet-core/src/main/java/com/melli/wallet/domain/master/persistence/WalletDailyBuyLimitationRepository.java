package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletDailyBuyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletDailyBuyLimitationRepository extends CrudRepository<WalletDailyBuyLimitationRedis, String> {

}
