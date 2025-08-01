package com.melli.wallet.domain.redis;

import com.melli.wallet.ConstantRedisName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;

@RedisHash(value = ConstantRedisName.WALLET_CASH_OUT_LIMITATION, timeToLive = 86400L) // 1 day
@Setter
@Getter
public class WalletCashOutLimitationRedis implements Serializable {

	@Id
	private String id; // walletAccountId + date

	private BigDecimal cashOutDailyAmount;
	private long cashOutDailyCount;
}
