package com.melli.wallet.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceObjectDTO {

	long balance = 0;
	long blockAmount = 0;

	public BalanceObjectDTO(Object[] columns) {
		this.balance = (columns[0] != null) ? (Long) columns[0] : 0;
		this.blockAmount = (columns[1] != null) ? (Long) columns[1] : 0;
	}

}
