package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Table(name = "complete_purchase_part")
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReportCompletePurchaseEntity extends ReportBaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channel_id", nullable = false)
	private ReportChannelEntity channelEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rrn_id", nullable = false)
	private ReportRrnEntity rrn;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccount;

	@Column(name = "transaction_time", nullable = false)
	private Date transactionTime;

	@Column(name = "amount")
	private long amount;

	//baraye eenke baraye create kardan bill rahat  tar bashim
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "merchant_id", nullable = false)
	private ReportMerchantEntity merchantEntity;

	@Column(name = "additional_data", nullable = true)
	private String additionalData;

	@Column(name = "settle_time", nullable = true)
	private Date settleTime;

	@Column(name = "settle_status", nullable = true)
	private String settleStatus;

	@Column(name = "mobile", nullable = true)
	private String mobile;

	@Column(name = "channel_code", nullable = true)
	private String channelCode;

	@Column(name = "terminal_bill_id" , nullable = true)
	private long terminalBillId;
} 