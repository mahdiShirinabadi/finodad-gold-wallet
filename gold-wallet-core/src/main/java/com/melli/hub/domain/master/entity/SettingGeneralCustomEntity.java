package com.melli.hub.domain.master.entity;

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
@Table(name = "setting_general_custom")
public class SettingGeneralCustomEntity extends BaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "setting_general_id", nullable = false)
	private SettingGeneralEntity settingGeneralEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_level_id", nullable = false)
	private WalletLevelEntity walletLevelEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_type_id", nullable = false)
	private WalletAccountTypeEntity walletAccountTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_currency_id", nullable = false)
	private WalletAccountCurrencyEntity walletAccountCurrencyEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_type_id", nullable = false)
	private WalletTypeEntity walletTypeEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = false)
	private ChannelEntity channelEntity;

	@Column(name = "value")
	private String value;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "end_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

}
