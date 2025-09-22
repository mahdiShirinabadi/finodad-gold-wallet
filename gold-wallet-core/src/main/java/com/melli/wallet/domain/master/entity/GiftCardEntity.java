package com.melli.wallet.domain.master.entity;

import com.melli.wallet.domain.enumaration.GiftCardStepStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Class Name: GiftCardEntity
 * Author: Mahdi Shirinabadi
 * Date: 9/22/2025
 */
@Entity
@Table(name = "gift_card")
@Setter
@Getter
@ToString
public class GiftCardEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "active_code", unique = true, nullable = false)
    private String activeCode;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "commission")
    private BigDecimal commission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rrn_id", nullable = false)
    private RrnEntity rrnEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_id", nullable = false)
    private WalletAccountEntity walletAccountEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gift_wallet_account_id", nullable = false)
    private WalletAccountEntity giftWalletAccountEntity;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GiftCardStepStatus status;

    @Column(name = "activated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date activatedAt;

    @Column(name = "activated_by")
    private String activatedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_wallet_account_id")
    private WalletAccountEntity destinationWalletAccountEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_currency_id", nullable = false)
    private WalletAccountCurrencyEntity walletAccountCurrencyEntity;

    @Column(name = "expire_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireAt;

    @Column(name = "nationalCode_by", nullable = false)
    private String nationalCodeBy;

}
