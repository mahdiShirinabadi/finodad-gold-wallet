package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
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

	@Column(name = "quantity", precision = 10, scale = 5)
	private BigDecimal quantity; // Purchased quantity

	@ManyToOne
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccount;

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

	@Column(name = "commission", length = 500)
	private String commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type")
	private TransactionTypeEnum transactionTypeEnum;

	@Transient
	private long totalAmount;

	@Transient
	private long terminalId;
}
