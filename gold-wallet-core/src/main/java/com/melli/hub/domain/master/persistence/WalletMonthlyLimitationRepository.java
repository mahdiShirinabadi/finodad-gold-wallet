package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.redis.WalletMonthlyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletMonthlyLimitationRepository extends CrudRepository<WalletMonthlyLimitationRedis, String> {

}
