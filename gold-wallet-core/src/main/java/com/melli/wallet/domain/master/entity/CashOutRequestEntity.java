package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cash_out_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class CashOutRequestEntity extends RequestEntity {

	@Column(name = "amount")
	private long amount;

	@Column(name = "iban")
	private String iban;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchantEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;

	@Column(name = "additional_data")
	private String additionalData;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_step")
    private SettlementStepEnum settlementStepEnum;

}
