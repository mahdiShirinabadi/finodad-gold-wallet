package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.IpDailyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpDailyLimitationRepository extends CrudRepository<IpDailyLimitationRedis, String> {

}
