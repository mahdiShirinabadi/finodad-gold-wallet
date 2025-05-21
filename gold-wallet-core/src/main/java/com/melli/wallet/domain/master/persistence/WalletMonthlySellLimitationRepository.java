package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletMonthlySellLimitationRepository extends CrudRepository<WalletMonthlySellLimitationRedis, String> {

}
