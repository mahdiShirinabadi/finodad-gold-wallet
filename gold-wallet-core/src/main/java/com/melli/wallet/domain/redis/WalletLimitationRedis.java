package com.melli.wallet.domain.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "WalletLimitation", timeToLive = 86400L) // 1 day
@Setter
@Getter
public class WalletLimitationRedis implements Serializable {

	@Id
	private String id; // walletAccountId + date

	private int purchaseDailyCount;
	private long purchaseDailyAmount;

	private int purchaseGoldDailyCount;
	private long purchaseGoldDailyAmount;

	private long cashInDailyAmount;
	private long cashInDailyCount;

	private long cashOutDailyAmount;
	private long cashOutDailyCount;

	private long p2PDailyAmount;
	private long p2PDailyCount;

}
