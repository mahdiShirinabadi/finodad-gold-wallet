package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "p_2_p_request")
@Setter
@Getter
@ToString
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class Person2PersonRequestEntity extends RequestEntity {


	@Column(name = "amount", precision = 10, scale = 5)
	private BigDecimal amount; // quantity

	@ManyToOne
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@ManyToOne
	@JoinColumn(name = "dest_wallet_account_id", nullable = false)
	private WalletAccountEntity destinationAccountWalletEntity;

	@ManyToOne
	@JoinColumn(name = "src_wallet_account_id", nullable = false)
	private WalletAccountEntity sourceAccountWalletEntity;

	@Column(name = "commission", length = 500)
	private BigDecimal commission;

	@Column(name = "additional_data", length = 500)
	private String additionalData;
}
