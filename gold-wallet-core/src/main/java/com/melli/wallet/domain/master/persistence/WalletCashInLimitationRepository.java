package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletCashInLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletCashInLimitationRepository extends CrudRepository<WalletCashInLimitationRedis, String> {

}
