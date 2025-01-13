package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "wallet_account")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WalletAccountEntity extends BaseEntityAudit implements Serializable {


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_id", nullable = false)
	private WalletEntity walletEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_type_id", nullable = false)
	private AccountTypeEntity accountTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_currency_id", nullable = false)
	private AccountCurrencyEntity accountCurrencyEntity;

	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "status")
	private int status;

	@Column(name = "pin")
	private String pin;

	@Column(name = "partner_id")
	private int partnerId;

	@Column(name = "end_time")
	private Date endTime;
}

