package com.melli.hub.domain.master.entity;

import com.melli.hub.domain.enumaration.CashInPaymentTypeEnum;
import com.melli.hub.domain.enumaration.TransactionTypeEnum;
import lombok.*;
import jakarta.persistence.*;

import java.util.Date;



@Entity
@Table(name = "cash_in_request")
@Setter
@Getter
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class CashInRequestEntity extends RequestEntity {
	public static String REF_NUMBER_USED = "used";
	public static String REF_NUMBER_FAIL = "fail";

	@Column(name = "amount")
	private long amount;

	@Column(name = "ref_number")
	private String refNumber;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccount;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@Column(name = "ref_number_status")
	private String refNumberStatus;

	@Column(name = "psp_token")
	private String channelToken;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "psp_response")
	private String channelResponse;

	@Column(name = "psp_request_time")
	private Date channelRequestTime;

	@Column(name = "psp_response_time")
	private Date channelResponseTime;

	@Enumerated(EnumType.STRING) // Store the enum as a string in the database
	@Column(name = "cash_in_payment_type", nullable = false, length = 50)
	private CashInPaymentTypeEnum cashInPaymentTypeEnum;
}
