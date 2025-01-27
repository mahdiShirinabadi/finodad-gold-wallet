package com.melli.hub.domain.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "WalletLimitation", timeToLive = 86400L) // 1 day
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPurchaseDailyCount() {
		return purchaseDailyCount;
	}

	public void setPurchaseDailyCount(int purchaseDailyCount) {
		this.purchaseDailyCount = purchaseDailyCount;
	}

	public long getPurchaseDailyAmount() {
		return purchaseDailyAmount;
	}

	public void setPurchaseDailyAmount(long purchaseDailyAmount) {
		this.purchaseDailyAmount = purchaseDailyAmount;
	}

	public long getCashInDailyAmount() {
		return cashInDailyAmount;
	}

	public void setCashInDailyAmount(long cashInDailyAmount) {
		this.cashInDailyAmount = cashInDailyAmount;
	}

	public long getCashInDailyCount() {
		return cashInDailyCount;
	}

	public void setCashInDailyCount(long cashInDailyCount) {
		this.cashInDailyCount = cashInDailyCount;
	}

	public long getCashOutDailyAmount() {
		return cashOutDailyAmount;
	}

	public void setCashOutDailyAmount(long cashOutDailyAmount) {
		this.cashOutDailyAmount = cashOutDailyAmount;
	}

	public long getCashOutDailyCount() {
		return cashOutDailyCount;
	}

	public void setCashOutDailyCount(long cashOutDailyCount) {
		this.cashOutDailyCount = cashOutDailyCount;
	}

	public long getP2PDailyAmount() {
		return p2PDailyAmount;
	}

	public void setP2PDailyAmount(long p2pDailyAmount) {
		p2PDailyAmount = p2pDailyAmount;
	}

	public long getP2PDailyCount() {
		return p2PDailyCount;
	}

	public void setP2PDailyCount(long p2pDailyCount) {
		p2PDailyCount = p2pDailyCount;
	}

	public int getPurchaseCreditDailyCount() {
		return purchaseCreditDailyCount;
	}

	public void setPurchaseCreditDailyCount(int purchaseCreditDailyCount) {
		this.purchaseCreditDailyCount = purchaseCreditDailyCount;
	}

	public long getPurchaseCreditDailyAmount() {
		return purchaseCreditDailyAmount;
	}

	public void setPurchaseCreditDailyAmount(long purchaseCreditDailyAmount) {
		this.purchaseCreditDailyAmount = purchaseCreditDailyAmount;
	}

}
