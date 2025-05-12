package com.melli.wallet.domain.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "WalletDailyBuyLimitation", timeToLive = 86400L) // 1 day
@Setter
@Getter
public class WalletDailyBuyLimitationRedis implements Serializable {

	@Id
	private String id; // walletAccountId + date

	private int count;
	private long amount;
}
