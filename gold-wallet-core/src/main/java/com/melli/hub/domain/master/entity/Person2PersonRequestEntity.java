package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "p_2_p_request")
@Setter
@Getter
@ToString
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class Person2PersonRequestEntity extends RequestEntity {


	@Column(name = "amount", precision = 10, scale = 5)
	private BigDecimal amount; // Purchased quantity

	@ManyToOne
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@ManyToOne
	@JoinColumn(name = "dest_wallet_account_id", nullable = false)
	private WalletAccountEntity destinationAccountWalletEntity;

	@ManyToOne
	@JoinColumn(name = "src_wallet_account_id", nullable = false)
	private WalletAccountEntity sourceAccountWalletEntity;
}
