package com.melli.wallet.domain.slave.entity;

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
public class ReportWalletAccountEntity extends ReportBaseEntityAudit implements Serializable {

	@Version
	@Column(name = "version")
	private Long version;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_id", nullable = false)
	private ReportWalletEntity walletEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_type_id", nullable = false)
	private ReportWalletAccountTypeEntity walletAccountTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_currency_id", nullable = false)
	private ReportWalletAccountCurrencyEntity walletAccountCurrencyEntity;

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