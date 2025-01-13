package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shirinabadi on 03/11/2016.
 *
 */
@Entity
@Table(name = "wallet")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WalletEntity extends BaseEntityAudit implements Serializable {

	@Column(name = "mobile")
	private String mobile;

	@Column(name = "national_code")
	private String nationalCode;

	@Column(name = "description")
	private String description;

	@Column(name = "end_time")
	private Date endTime;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_channel_id", nullable = false)
	private ChannelEntity owner;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_type_id", nullable = false)
	private WalletTypeEntity walletTypeEntity;

	@Column(name = "status")
	private int status;

	@Column(name = "identifier")
	private String identifier;

	@Column(name = "level")
	private int level;
}
