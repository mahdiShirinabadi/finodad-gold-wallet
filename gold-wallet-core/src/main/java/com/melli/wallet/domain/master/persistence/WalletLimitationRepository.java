package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.WalletLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletLimitationRepository extends CrudRepository<WalletLimitationRedis, String> {

}
