package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.redis.IpDailyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportIpDailyLimitationRepository extends CrudRepository<IpDailyLimitationRedis, String> {

} 