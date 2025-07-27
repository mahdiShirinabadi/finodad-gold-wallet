package com.melli.wallet.domain.slave.entity;

import com.melli.wallet.domain.enumaration.CashInPaymentTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cash_in_request")
@Setter
@Getter
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class ReportCashInRequestEntity extends ReportRequestEntity {
	public static String REF_NUMBER_USED = "used";
	public static String REF_NUMBER_FAIL = "fail";

	@Column(name = "amount")
	private long amount;

	@Column(name = "ref_number")
	private String refNumber;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccount;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private ReportRrnEntity rrnEntity;

	@Column(name = "additional_data")
	private String additionalData;

	@Enumerated(EnumType.STRING) // Store the enum as a string in the database
	@Column(name = "cash_in_payment_type", nullable = false, length = 50)
	private CashInPaymentTypeEnum cashInPaymentTypeEnum;
} 