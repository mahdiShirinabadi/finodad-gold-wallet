package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "wallet_iban")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ReportWalletIbanEntity extends ReportBaseEntityAudit implements Serializable {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private ReportWalletAccountEntity walletAccount;

	@Column(name = "national_code")
	private String nationalCode;

	@Column(name = "birth_date")
	private String birthDate;

	@Column(name = "iban")
	private String iban;
	
	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "account_bank_name")
	private String accountBankName;

	@Column(name = "account_owner")
	private String accountOwner;
} 