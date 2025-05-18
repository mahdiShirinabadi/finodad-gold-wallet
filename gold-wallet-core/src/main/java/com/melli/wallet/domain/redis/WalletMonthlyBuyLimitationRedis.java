package com.melli.wallet.domain.redis;

import com.melli.wallet.ConstantRedisName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = ConstantRedisName.WALLET_BUY_MONTHLY_LIMITATION, timeToLive = 2678400L) // 1 day
@Setter
@Getter
public class WalletMonthlyBuyLimitationRedis implements Serializable {

	@Id
	private String id; // walletAccountId + date

	private int count;
	private long amount;

}
