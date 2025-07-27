package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "settlement")
public class ReportSettlementEntity extends ReportBaseEntityAudit implements Serializable {

    @Column(name = "request_id", nullable = false)
    private long cashOutRequestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_id", nullable = false)
    private ReportWalletAccountEntity walletAccount;

    @Column(name = "destination_iban")
    private String destinationIban;

    @Column(name = "destination_account")
    private String destinationAccount;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "source_account")
    private String sourceAccount;

    @Column(name = "amount")
    private long amount;

    @Column(name = "channel_request_time")
    private Date channelRequestTime;

    @Column(name = "channel_response_time")
    private Date channelResponseTime;

    @Column(name = "channel_ref_number")
    private String channelRefNumber;

    @Column(name = "channel_response")
    private String channelResponse;

    @Column(name = "description")
    private String description;

    @Column(name = "settlement_date")
    private String settlementDate;

    @Column(name = "paya_inquiry")
    private boolean payaInquiry;

    @Column(name = "paya_inquiry_result")
    private Integer payaInquiryResult;

    @Column(name = "paya_inquiry_message")
    private String payaInquiryMessage;

    @Column(name = "channel_result")
    private String channelResult;
} 