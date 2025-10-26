package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sell_collateral_request")
@Setter
@Getter
public class ReportSellCollateralRequestEntity extends ReportBaseEntityAudit {

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "wallet_account_id")
    private Long walletAccountId;

    @Column(name = "rrn_id")
    private Long rrnId;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "create_collateral_request_id")
    private Long createCollateralRequestId;

    @Column(name = "cash_out_request_id")
    private Long cashOutRequestId;

    @Column(name = "additional_data")
    private String additionalData;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "commission")
    private BigDecimal commission;

    @Column(name = "iban")
    private String iban;

    @Column(name = "price")
    private Long price;
}
