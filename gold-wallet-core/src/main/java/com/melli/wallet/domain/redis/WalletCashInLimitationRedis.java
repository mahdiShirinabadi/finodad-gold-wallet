package com.melli.wallet.domain.redis;

import com.melli.wallet.ConstantRedisName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;

@RedisHash(value = ConstantRedisName.WALLET_CASH_IN_LIMITATION, timeToLive = 86400L) // 1 day
@Setter
@Getter
public class WalletCashInLimitationRedis implements Serializable {

	@Id
	private String id; // walletAccountId + date

	private BigDecimal cashInDailyAmount;
	private long cashInDailyCount;
}
