package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.redis.WalletLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletLimitationRepository extends CrudRepository<WalletLimitationRedis, String> {

}
