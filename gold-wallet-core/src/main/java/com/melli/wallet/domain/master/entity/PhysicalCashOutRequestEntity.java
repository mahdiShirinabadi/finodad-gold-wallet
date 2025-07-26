package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "physical_cash_out_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class PhysicalCashOutRequestEntity extends RequestEntity {

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "final_quantity") //quantity - commission
	private BigDecimal finalQuantity;

	@Column(name = "commission", length = 500)
	private BigDecimal commission;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@Column(name = "additional_data")
	private String additionalData;
}
