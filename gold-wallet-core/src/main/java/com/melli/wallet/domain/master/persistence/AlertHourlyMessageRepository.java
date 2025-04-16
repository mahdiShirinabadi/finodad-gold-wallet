package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.AlertHourlyMessageRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertHourlyMessageRepository extends CrudRepository<AlertHourlyMessageRedis, String> {
}
