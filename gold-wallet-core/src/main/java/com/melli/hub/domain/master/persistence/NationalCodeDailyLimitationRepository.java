package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.redis.NationalCodeDailyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NationalCodeDailyLimitationRepository extends CrudRepository<NationalCodeDailyLimitationRedis, String> {

}
