package com.melli.wallet.domain.master.entity;

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
public class LimitationGeneralCustomEntity extends BaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "limitation_general_id", nullable = false)
	private LimitationGeneralEntity limitationGeneralEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_level_id", nullable = true)
	private WalletLevelEntity walletLevelEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_type_id", nullable = true)
	private WalletAccountTypeEntity walletAccountTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_currency_id", nullable = true)
	private WalletAccountCurrencyEntity walletAccountCurrencyEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_type_id", nullable = true)
	private WalletTypeEntity walletTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = true)
	private ChannelEntity channelEntity;

	@Column(name = "value")
	private String value;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "end_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

}
