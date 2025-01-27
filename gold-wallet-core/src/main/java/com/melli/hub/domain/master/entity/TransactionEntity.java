package com.melli.hub.domain.master.entity;

import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "transaction_part")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TransactionEntity extends BaseEntityAudit implements Serializable {

	public static final String DEPOSIT = "D";
	public static final String WITHDRAW = "W";

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@Column(name = "amount", nullable = false)
	private long amount;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "balance", nullable = false)
	private long balance;

	@Column(name = "description")
	private String description;

	@Column(name = "additional_data", nullable = true)
	private String additionalData;

	@Column(name = "request_type_id", nullable = true)
	private Long requestTypeId;
}