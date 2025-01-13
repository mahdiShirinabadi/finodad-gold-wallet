package com.melli.hub.domain.master.entity;

import lombok.Getter;
import lombok.Setter;

import lombok.*;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cash_in_special_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class CashInSpecialRequestEntity extends RequestEntity {

	@Column(name = "amount")
	private long amount;

	@Column(name = "ref_number")
	private String refNumber;

	@Column(name = "additional_data")
	private String additionalData;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;
}
