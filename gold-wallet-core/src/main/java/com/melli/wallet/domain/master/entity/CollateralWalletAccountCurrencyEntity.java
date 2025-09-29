package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "collateral_wallet_account_currency")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CollateralWalletAccountCurrencyEntity extends BaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collateral_id", nullable = false)
    private CollateralEntity collateralEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_currency_id", nullable = false)
    private WalletAccountCurrencyEntity walletAccountCurrencyEntity;
}
