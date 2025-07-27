package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shahkar_info")
public class ReportShahkarInfoEntity extends ReportBaseEntityAudit implements Serializable {

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "channel_request_time", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date channelRequestTime;

    @Column(name = "channel_response_time", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date channelResponseTime;

    @Column(name = "channel_response")
    private String channelResponse;

    @Column(name = "is_match")
    private Boolean isMatch;

} 