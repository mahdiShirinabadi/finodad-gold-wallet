package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "cash_in_ipg_history_request")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CashInIpgHistoryRequestEntity extends BaseEntityAudit implements Serializable {

	@OneToOne
	@JoinColumn(name = "cash_in_ipg_request", nullable = false)
	private CashInWithIpgRequestEntity cashInWithIpgRequestEntity;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "psp_response")
	private String channelResponse;

	@Column(name = "ref_number")
	private String refNumber;

	@Column(name = "psp_request_time")
	private Date channelRequestTime;

	@Column(name = "psp_response_time")
	private Date channelResponseTime;
}
