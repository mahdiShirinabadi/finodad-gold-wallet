package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "limitation_general_custom")
public class ReportLimitationGeneralCustomEntity extends ReportBaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "limitation_general_id", nullable = false)
	private ReportLimitationGeneralEntity limitationGeneralEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_level_id", nullable = true)
	private ReportWalletLevelEntity walletLevelEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_type_id", nullable = true)
	private ReportWalletAccountTypeEntity walletAccountTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_currency_id", nullable = true)
	private ReportWalletAccountCurrencyEntity walletAccountCurrencyEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_type_id", nullable = true)
	private ReportWalletTypeEntity walletTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = true)
	private ReportChannelEntity channelEntity;

	@Column(name = "value")
	private String value;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "end_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

} 