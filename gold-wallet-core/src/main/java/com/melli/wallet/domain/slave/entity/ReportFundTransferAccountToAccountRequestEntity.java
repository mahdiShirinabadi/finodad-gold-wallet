package com.melli.wallet.domain.slave.entity;

import com.melli.wallet.domain.master.entity.RequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "fund_transfer_account_to_account_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class ReportFundTransferAccountToAccountRequestEntity extends ReportRequestEntity {

    @Column(name = "from_account", nullable = false)
    private String fromAccount;

    @Column(name = "to_account", nullable = false)
    private String toAccount;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rrn_id", nullable = false)
    private ReportRrnEntity rrnEntity;

    @Column(name="ref_number")
    private String refNumber;

    @Column(name="trace_number")
    private String traceNumber;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "channel_request_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date channelRequestTime;

    @Column(name = "channel_response")
    private String channelResponse;

    @Column(name = "channel_result")
    private String channelResult;

    @Column(name = "channel_response_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date channelResponseTime;

    @Column(name = "additional_data")
    private String additionalData;

    @Column(name = "multi_transaction")
    private Boolean multiTransaction;


}
