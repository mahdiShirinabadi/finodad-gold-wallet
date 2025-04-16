package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "settlement")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class IbanSettlementEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "request_id", nullable = false)
    private long cashOutRequestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_account_id", nullable = false)
    private WalletAccountEntity walletAccountEntity;

    @Column(name = "iban")
    private String destIban;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "source_account")
    private String sourceAccount;

    @Column(name = "cif")
    private String cif;

    @Column(name = "account")
    private String destAccount;

    @Column(name = "amount")
    private long amount;

    @Column(name = "channel_request_time")
    private Date channelRequestTime;

    @Column(name = "channel_response_time")
    private Date channelResponseTime;

    @Column(name = "channel_ref_number")
    private String channelRefNumber;

    @Column(name = "channel_seq_number")
    private String channelSeqNumber;

    @Column(name = "channel_result")
    private String channelResult;

    @Column(name = "channel_message")
    private String channelMessage;

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
}
