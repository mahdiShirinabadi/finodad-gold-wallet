package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.NationalCodeDailyLimitationRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NationalCodeDailyLimitationRepository extends CrudRepository<NationalCodeDailyLimitationRedis, String> {

}
