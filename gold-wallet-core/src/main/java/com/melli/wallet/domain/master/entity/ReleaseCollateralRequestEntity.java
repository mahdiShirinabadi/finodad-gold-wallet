package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "release_collateral_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReleaseCollateralRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "create_collateral_request_id", nullable = false)
	private CreateCollateralRequestEntity createCollateralRequestEntity;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "commission")
	private BigDecimal commission;
}
