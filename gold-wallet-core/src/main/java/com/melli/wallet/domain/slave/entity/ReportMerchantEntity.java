package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "merchant")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportMerchantEntity extends ReportBaseEntityAudit implements Serializable {

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "mobile", nullable = false)
	private String mobile;

	@Column(name = "national_code", nullable = false)
	private String nationalCode;

	@Column(name = "economical_code")
	private String economicalCode;

	@Column(name = "logo")
	private String logo;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_id", nullable = false)
	private ReportWalletEntity walletEntity;

	@Column(name = "settlement_type", nullable = false)
	private int settlementType;

	@Column(name = "end_time")
	private Date endTime;

	@Column(name = "pay_id")
	private String payId;

	@Column(name = "status")
	private int status;
} 