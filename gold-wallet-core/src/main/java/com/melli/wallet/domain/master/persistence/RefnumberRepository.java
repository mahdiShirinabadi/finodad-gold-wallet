package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.IpDailyLimitationRedis;
import com.melli.wallet.domain.redis.RefNumberRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefnumberRepository extends CrudRepository<RefNumberRedis, String> {

}
