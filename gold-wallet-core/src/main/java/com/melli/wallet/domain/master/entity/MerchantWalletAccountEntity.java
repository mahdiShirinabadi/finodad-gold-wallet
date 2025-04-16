package com.melli.wallet.domain.master.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "merchant_wallet_account")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class MerchantWalletAccountEntity extends BaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantEntity merchantEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_type_id", nullable = false)
	private WalletAccountTypeEntity walletAccountTypeEntity;

	@Column(name = "end_time")
	private Date endTime;

	@Column(name = "status")
	private int status;

	@Column(name = "name")
	private String name;
}
