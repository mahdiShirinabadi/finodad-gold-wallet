package com.melli.hub.domain.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "WalletMonthlyLimitation", timeToLive = 2678400L) // 1 month
public class WalletMonthlyLimitationRedis implements Serializable {

	@Id
	private String id; // mobileNumber+merchantCode+monthly+numberMonthOfYear

	private int purchaseMonthlyCount;
	private long purchaseMonthlyAmount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPurchaseMonthlyCount() {
		return purchaseMonthlyCount;
	}

	public void setPurchaseMonthlyCount(int purchaseMonthlyCount) {
		this.purchaseMonthlyCount = purchaseMonthlyCount;
	}

	public long getPurchaseMonthlyAmount() {
		return purchaseMonthlyAmount;
	}

	public void setPurchaseMonthlyAmount(long purchaseMonthlyAmount) {
		this.purchaseMonthlyAmount = purchaseMonthlyAmount;
	}
}
