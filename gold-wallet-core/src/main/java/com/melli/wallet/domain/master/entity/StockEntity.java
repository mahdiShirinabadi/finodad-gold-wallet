package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Table(name = "stock")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class StockEntity extends BaseEntityAudit implements Serializable {

	@Column(name = "name")
	private String name;

	@Column(name = "code")
	private String code; //for heavy transaction with use 2 last number from national code 00-99

	@ManyToOne
	@JoinColumn(name = "wallet_account_currency_id", nullable = false)
	private WalletAccountCurrencyEntity walletAccountCurrencyEntity;
}
