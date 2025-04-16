package com.melli.wallet.domain.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "ipDaily", timeToLive = 86400L)
@Setter
@Getter
public class IpDailyLimitationRedis implements Serializable {

    @Id
    private String id; // ip + date
    private int ipCount;
}
