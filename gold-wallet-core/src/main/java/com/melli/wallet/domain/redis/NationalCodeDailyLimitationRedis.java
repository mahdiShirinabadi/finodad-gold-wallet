package com.melli.wallet.domain.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "nationalCodeDaily", timeToLive = 86400L)
@Setter
@Getter
public class NationalCodeDailyLimitationRedis implements Serializable {

    @Id
    private String id; // nationalCode + date
    private int nationalCodeCount;
}
