package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "merchant_wallet_account")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportMerchantWalletAccountEntity extends ReportBaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "merchant_id", nullable = false)
	private ReportMerchantEntity merchantEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccountEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_type_id", nullable = false)
	private ReportWalletAccountTypeEntity walletAccountTypeEntity;

	@Column(name = "end_time")
	private Date endTime;

	@Column(name = "status")
	private int status;

	@Column(name = "name")
	private String name;
} 