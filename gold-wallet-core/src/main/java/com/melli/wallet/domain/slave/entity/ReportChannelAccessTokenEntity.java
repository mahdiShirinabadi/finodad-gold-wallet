package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "channel_access_token")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportChannelAccessTokenEntity extends ReportBaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id", nullable = false)
    private ReportChannelEntity channelEntity;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "ip", nullable = false, unique = true)
    private String ip;

    @Column(name = "access_token_expire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date accessTokenExpireTime;

    @Column(name = "refresh_token_expire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date refreshTokenExpireTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
} 