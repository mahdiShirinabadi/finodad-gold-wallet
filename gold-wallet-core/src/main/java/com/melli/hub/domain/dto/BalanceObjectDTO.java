package com.melli.hub.domain.dto;

public class BalanceObjectDTO {

	long balance = 0;
	long blockAmount = 0;

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public long getBlockAmount() {
		return blockAmount;
	}

	public void setBlockAmount(long blockAmount) {
		this.blockAmount = blockAmount;
	}

	public BalanceObjectDTO(Object[] columns) {
		this.balance = (columns[0] != null) ? (Long) columns[0] : 0;
		this.blockAmount = (columns[1] != null) ? (Long) columns[1] : 0;
	}

}
