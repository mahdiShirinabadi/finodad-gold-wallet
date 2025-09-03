package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletDailyP2pLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletDailyP2PLimitationRepository extends CrudRepository<WalletDailyP2pLimitationRedis, String> {

}
