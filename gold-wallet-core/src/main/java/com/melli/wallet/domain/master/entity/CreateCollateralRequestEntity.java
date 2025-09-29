package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.CollateralStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "create_collateral_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CreateCollateralRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "collateral_id", nullable = false)
	private CollateralEntity collateralEntity;

	@Column(name = "code")
	private String code;

	@Column(name = "additional_data")
	private String additionalData;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private CollateralStatusEnum collateralStatusEnum;

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "final_block_quantity")
	private BigDecimal finalBlockQuantity;

	@Column(name = "commission")
	private BigDecimal commission;
}
