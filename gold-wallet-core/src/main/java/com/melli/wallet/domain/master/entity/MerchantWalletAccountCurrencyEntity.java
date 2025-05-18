package com.melli.wallet.domain.master.entity;

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
public class MerchantWalletAccountCurrencyEntity extends BaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchantEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_currency_id", nullable = false)
    private WalletAccountCurrencyEntity walletAccountCurrencyEntity;
}
