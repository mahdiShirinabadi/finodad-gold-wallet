package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_history")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class StockHistoryEntity extends BaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "stock_id", nullable = false)
	private StockEntity stockEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "transaction_id", nullable = false)
	private TransactionEntity transactionEntity;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "balance", nullable = false)
	private BigDecimal balance;
}
