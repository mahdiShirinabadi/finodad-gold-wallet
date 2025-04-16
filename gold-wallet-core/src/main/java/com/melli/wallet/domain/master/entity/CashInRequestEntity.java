package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.CashInPaymentTypeEnum;
import lombok.*;
import jakarta.persistence.*;


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

	@Column(name = "additional_data")
	private String additionalData;

	@Enumerated(EnumType.STRING) // Store the enum as a string in the database
	@Column(name = "cash_in_payment_type", nullable = false, length = 50)
	private CashInPaymentTypeEnum cashInPaymentTypeEnum;
}
