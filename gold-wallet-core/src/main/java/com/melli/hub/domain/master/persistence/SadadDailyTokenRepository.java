package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.redis.SadadDailyTokenRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SadadDailyTokenRepository extends CrudRepository<SadadDailyTokenRedis, String> {

}
