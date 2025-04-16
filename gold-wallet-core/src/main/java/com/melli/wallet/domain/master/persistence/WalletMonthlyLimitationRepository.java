package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletMonthlyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletMonthlyLimitationRepository extends CrudRepository<WalletMonthlyLimitationRedis, String> {

}
