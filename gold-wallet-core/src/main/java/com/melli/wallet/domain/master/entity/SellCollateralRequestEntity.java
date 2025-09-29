package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.LiquidCollateralStepEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "sell_collateral_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class SellCollateralRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "create_collateral_request_id", nullable = false)
	private CreateCollateralRequestEntity createCollateralRequestEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relase_collateral_request_id")
	private ReleaseCollateralRequestEntity releaseCollateralRequestEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "purchase_request_id")
	private PurchaseRequestEntity purchaseRequestEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cash_out_request_id")
	private CashOutRequestEntity cashOutRequestEntity;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "commission")
	private BigDecimal commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "step")
	private LiquidCollateralStepEnum collateralStepEnum;

	@Column(name = "iban")
	private String iban;

	@Column(name = "price")
	private Long price;
}
