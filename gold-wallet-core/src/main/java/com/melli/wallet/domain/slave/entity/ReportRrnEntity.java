package com.melli.wallet.domain.slave.entity;

import com.melli.wallet.domain.master.RrnExtraData;
import com.melli.wallet.domain.master.RrnExtraDataConvertor;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Created by shirinabadi on 03/11/2016.
 *
 */
@Entity
@Table(name = "rrn")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ReportRrnEntity extends ReportBaseEntityAudit implements Serializable {

	@Column(name = "national_code")
	private String nationalCode;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = false)
	private ReportChannelEntity channel;

	@Column(name = "uuid", insertable = false, updatable = false)
	private String uuid;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "request_type_id", nullable = false)
	private ReportRequestTypeEntity requestTypeEntity;

	@Convert(converter = RrnExtraDataConvertor.class)
	@Column(name = "extra_data")
	private RrnExtraData extraData;

} 