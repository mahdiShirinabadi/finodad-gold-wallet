package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.redis.AlertHourlyMessageRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertHourlyMessageRepository extends CrudRepository<AlertHourlyMessageRedis, String> {
}
