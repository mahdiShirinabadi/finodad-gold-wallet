package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ShirinAbadi.Mahdi on 8/29/2022
 * @project wallet-api-multi-purchase
 */
@Entity
@Table(name = "escrow_wallet_account")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class EscrowWalletAccountEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "wallet_account_id")
    private int walletAccountId;

    @Column(name = "wallet_account_number")
    private String walletAccountNumber;

    @Column(name = "wallet_account_currency_id")
    private int walletAccountCurrencyId;

    @Column(name = "wallet_id")
    private int walletId;
}
