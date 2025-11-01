package com.melli.wallet.domain.slave.entity;

import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cash_out_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class ReportCashOutRequestEntity extends ReportRequestEntity {

    @Column(name = "amount")
    private long amount;

    @Column(name = "iban")
    private String iban;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_id", nullable = false)
    private ReportWalletAccountEntity walletAccountEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    private ReportMerchantEntity merchantEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rrn_id", nullable = false)
    private ReportRrnEntity rrnEntity;

    @Column(name = "additional_data")
    private String additionalData;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_step")
    private SettlementStepEnum settlementStepEnum;
} 