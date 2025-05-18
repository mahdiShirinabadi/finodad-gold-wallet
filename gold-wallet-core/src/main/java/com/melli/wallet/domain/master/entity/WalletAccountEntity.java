package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "wallet_account")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WalletAccountEntity extends BaseEntityAudit implements Serializable {

	@Version
	@Column(name = "version")
	private Long version;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_id", nullable = false)
	private WalletEntity walletEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_type_id", nullable = false)
	private WalletAccountTypeEntity walletAccountTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_currency_id", nullable = false)
	private WalletAccountCurrencyEntity walletAccountCurrencyEntity;

	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private WalletStatusEnum status;

	@Column(name = "pin")
	private String pin;

	@Column(name = "end_time")
	private Date endTime;
}

