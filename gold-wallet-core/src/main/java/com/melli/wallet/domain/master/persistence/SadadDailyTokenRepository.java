package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.redis.SadadDailyTokenRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SadadDailyTokenRepository extends CrudRepository<SadadDailyTokenRedis, String> {

}
