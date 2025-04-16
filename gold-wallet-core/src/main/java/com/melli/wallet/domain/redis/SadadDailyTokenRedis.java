package com.melli.wallet.domain.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "sadadDailyToken", timeToLive = 86400L)
@Setter
@Getter
public class SadadDailyTokenRedis {

    @Id
    private String scope; // scope
    private long expireTime;
    private String token;
}
