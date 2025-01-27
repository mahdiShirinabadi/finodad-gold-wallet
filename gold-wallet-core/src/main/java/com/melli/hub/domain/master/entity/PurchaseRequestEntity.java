package com.melli.hub.domain.master.entity;

import com.melli.hub.domain.enumaration.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
public class PurchaseRequestEntity extends RequestEntity {

	@Column(name = "price")
	private Long price; // Purchased quantity price

	@Column(name = "amount", precision = 10, scale = 5)
	private BigDecimal amount; // Purchased quantity

	@ManyToOne
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccount;

	@ManyToOne
	@JoinColumn(name = "escrow_wallet_account_id", nullable = false)
	private EscrowWalletAccountEntity escrowWalletAccount;

	@ManyToOne
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@ManyToOne
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantEntity merchantEntity;

	@Column(name = "national_code", length = 100)
	private String nationalCode;

	@Column(name = "terminal_amount", length = 100)
	private String terminalAmount;

	@Column(name = "additional_data", length = 500)
	private String additionalData;

	@Column(name = "ref_number", length = 500)
	private String refNumber;

	@Column(name = "commission_amount", precision = 10, scale = 5)
	private BigDecimal commissionAmount;

	@Column(name = "commission_merchant_amount", precision = 10, scale = 5)
	private BigDecimal commissionMerchantAmount;

	@Column(name = "commission_channel_amount", precision = 10, scale = 5)
	private BigDecimal commissionChannelAmount;

	@Column(name = "commission_finodad_amount", precision = 10, scale = 5)
	private BigDecimal commissionFinodadAmount;

	@Column(name = "commission_percent", precision = 10, scale = 5)
	private BigDecimal commissionPercent; // Instant commission percentage

	@Column(name = "commission_merchant_percent", precision = 10, scale = 5)
	private BigDecimal commissionMerchantPercent; // Instant merchant commission percentage

	@Column(name = "commission_channel_percent", precision = 10, scale = 5)
	private BigDecimal commissionChannelPercent; // Instant channel commission percentage

	@Column(name = "commission_finodad_percent", precision = 10, scale = 5)
	private BigDecimal commissionFinodadPercent; // Instant finodad commission percentage

	@Enumerated(EnumType.STRING) // Store the enum as a string in the database
	@Column(name = "transaction_type", nullable = false, length = 50)
	private TransactionTypeEnum transactionType;

	@Transient
	private long totalAmount;

	@Transient
	private long terminalId;
}
