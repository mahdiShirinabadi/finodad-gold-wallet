package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletCashOutLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletCashOutLimitationRepository extends CrudRepository<WalletCashOutLimitationRedis, String> {

}
