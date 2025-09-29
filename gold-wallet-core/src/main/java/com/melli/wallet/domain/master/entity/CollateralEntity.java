package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "collateral")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CollateralEntity extends BaseEntityAudit implements Serializable {

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "mobile", nullable = false)
	private String mobile;

	@Column(name = "economical_code")
	private String economicalCode;

	@Column(name = "logo")
	private String logo;

	@Column(name = "iban")
	private String iban;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_id", nullable = false)
	private WalletEntity walletEntity;

	@Column(name = "end_time")
	private Date endTime;

	@Column(name = "pay_id")
	private String payId;

	@Column(name = "status")
	private int status;
}
