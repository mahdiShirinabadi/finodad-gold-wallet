package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "purchase_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
public class PurchaseRequestEntity extends RequestEntity {

	@Column(name = "amount")
	private long amount;

	@Column(name = "terminal_amount", nullable = true)
	private String terminalAmount;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrn;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccount;

	@Column(name = "escrow_wallet_account_id", nullable = true)
	private Integer escrowWalletAccountId;

	//baraye eenke baraye create kardan bill rahat  tar bashim
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_type_id", nullable = false)
	private AccountTypeEntity accountType;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantEntity merchant;

	@Column(name = "additional_data", nullable = true)
	private String additionalData;

	@Column(name = "channel_code", nullable = true)
	private String channelCode;

	@Column(name = "ref_number", nullable = true)
	private String refNumber;

	@Column(name = "amount_cashback")
	private Long amountCashback;

	@Transient
	private long totalAmount;

	@Transient
	private long terminalId;
}
