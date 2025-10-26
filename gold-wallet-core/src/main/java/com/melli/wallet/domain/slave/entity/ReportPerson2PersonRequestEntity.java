package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "p_2_p_request")
@Setter
@Getter
public class ReportPerson2PersonRequestEntity extends ReportBaseEntityAudit {

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "final_amount")
    private BigDecimal finalAmount;

    @Column(name = "rrn_id")
    private Long rrnId;

    @Column(name = "dest_wallet_account_id")
    private Long destWalletAccountId;

    @Column(name = "src_wallet_account_id")
    private Long srcWalletAccountId;

    @Column(name = "commission")
    private BigDecimal commission;

    @Column(name = "additional_data")
    private String additionalData;
}
