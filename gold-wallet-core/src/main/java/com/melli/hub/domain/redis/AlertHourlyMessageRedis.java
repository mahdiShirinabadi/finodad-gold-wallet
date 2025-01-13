package com.melli.hub.domain.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "alertHourlyMessage", timeToLive = 3600L)
@Setter
@Getter
@AllArgsConstructor
public class AlertHourlyMessageRedis {

    @Id
    private String key; // mobile+errorCode
    private String message;
    private String mobile;
    private String errorCode;
}
