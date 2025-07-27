package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Entity
@Table(name = "request")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RequestEntity extends BaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = false)
	private ChannelEntity channel;

	@Column(name = "result")
	private int result;

	@Column(name = "channel_ip", nullable = false)
	private String channelIp;

	@Column(name = "customer_ip", nullable = true)
	private String customerIp;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "request_type_id", nullable = false)
	private RequestTypeEntity requestTypeEntity;
}
