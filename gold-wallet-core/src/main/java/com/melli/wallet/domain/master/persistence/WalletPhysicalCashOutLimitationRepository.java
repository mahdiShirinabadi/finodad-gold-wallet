package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletPhysicalCashOutLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletPhysicalCashOutLimitationRepository extends CrudRepository<WalletPhysicalCashOutLimitationRedis, String> {

}
