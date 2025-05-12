package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletMonthlyBuyLimitationRepository extends CrudRepository<WalletMonthlyBuyLimitationRedis, String> {

}
