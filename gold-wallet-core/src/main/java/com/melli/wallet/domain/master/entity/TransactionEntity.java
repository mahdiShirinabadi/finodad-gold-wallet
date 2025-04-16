package com.melli.wallet.domain.master.entity;

import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.*;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;

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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@Column(name = "amount", nullable = false)
	private double amount;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "balance", nullable = false)
	private double balance;

	@Column(name = "description")
	private String description;

	@Column(name = "additional_data", nullable = true)
	private String additionalData;

	@Column(name = "request_type_id", nullable = true)
	private Long requestTypeId;
}