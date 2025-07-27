package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "merchant_wallet_account_currency")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportMerchantWalletAccountCurrencyEntity extends ReportBaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    private ReportMerchantEntity merchantEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_currency_id", nullable = false)
    private ReportWalletAccountCurrencyEntity walletAccountCurrencyEntity;
} 