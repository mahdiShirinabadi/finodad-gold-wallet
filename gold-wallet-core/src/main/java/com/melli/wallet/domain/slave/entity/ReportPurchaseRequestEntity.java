package com.melli.wallet.domain.slave.entity;

import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_request")
@Setter
@Getter
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class ReportPurchaseRequestEntity extends ReportRequestEntity {

	@Column(name = "price")
	private Long price; // Purchased quantity price

	@Column(name = "quantity", precision = 10, scale = 5)
	private BigDecimal quantity; // Purchased quantity

	@ManyToOne
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccount;

	@ManyToOne
	@JoinColumn(name = "rrn_id", nullable = false)
	private ReportRrnEntity rrnEntity;

	@ManyToOne
	@JoinColumn(name = "merchant_id", nullable = false)
	private ReportMerchantEntity merchantEntity;

	@Column(name = "national_code", length = 100)
	private String nationalCode;

	@Column(name = "terminal_amount", length = 100)
	private BigDecimal terminalAmount;

	@Column(name = "additional_data", length = 500)
	private String additionalData;

	@Column(name = "ref_number", length = 500)
	private String refNumber;

	@Column(name = "commission", length = 500)
	private BigDecimal commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type")
	private TransactionTypeEnum transactionTypeEnum;
} 