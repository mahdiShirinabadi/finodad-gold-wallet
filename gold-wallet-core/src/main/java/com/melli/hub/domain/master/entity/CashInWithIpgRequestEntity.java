package com.melli.hub.domain.master.entity;

import lombok.*;
import jakarta.persistence.*;

import java.util.Date;



@Entity
@Table(name = "cash_in_ipg_request")
@Setter
@Getter
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class CashInWithIpgRequestEntity extends RequestEntity {
	public static String REF_NUMBER_USED = "used";
	public static String REF_NUMBER_FAIL = "fail";

	@Column(name = "amount")
	private long amount;

	@Column(name = "ref_number")
	private String refNumber;

	@Column(name = "ref_number_status")
	private String refNumberStatus;

	@Column(name = "channel_token")
	private String channelToken;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "channel_response")
	private String channelResponse;

	@Column(name = "channel_request_time")
	private Date channelRequestTime;

	@Column(name = "channel_response_time")
	private Date channelResponseTime;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccount;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;
}
