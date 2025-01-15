package com.melli.hub.domain.master.entity;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "complete_purchase_part")
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CompletePurchaseEntity extends BaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channel_id", nullable = false)
	private ChannelEntity channelEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrn;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccount;

	@Column(name = "transaction_time", nullable = false)
	private Date transactionTime;

	@Column(name = "amount")
	private long amount;

	//baraye eenke baraye create kardan bill rahat  tar bashim
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantEntity merchantEntity;

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
