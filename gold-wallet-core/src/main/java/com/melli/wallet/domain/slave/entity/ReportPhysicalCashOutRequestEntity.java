package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "physical_cash_out_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class ReportPhysicalCashOutRequestEntity extends ReportRequestEntity {

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "final_quantity") //quantity - commission
	private BigDecimal finalQuantity;

	@Column(name = "commission", length = 500)
	private BigDecimal commission;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private ReportRrnEntity rrnEntity;

	@Column(name = "additional_data")
	private String additionalData;
} 