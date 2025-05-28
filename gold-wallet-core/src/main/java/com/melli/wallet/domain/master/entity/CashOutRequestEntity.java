package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cash_out_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class CashOutRequestEntity extends RequestEntity {

	@Column(name = "amount")
	private long amount;

	@Column(name = "iban")
	private String iban;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@Column(name = "national_code")
	private String nationalCode;

	@Column(name = "birth_date")
	private String birthDate;

	@Column(name = "settlement_status")
	private String settlementStatus;

	@Column(name = "additional_data")
	private String additionalData;
}
