package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "transaction_part")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ReportTransactionEntity extends ReportBaseEntityAudit implements Serializable {

	public static final String DEPOSIT = "D";
	public static final String WITHDRAW = "W";

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private ReportRrnEntity rrnEntity;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "balance", nullable = false)
	private BigDecimal balance;

	@Column(name = "description")
	private String description;

	@Column(name = "additional_data", nullable = true)
	private String additionalData;

	@Column(name = "request_type_id", nullable = true)
	private Long requestTypeId;
} 