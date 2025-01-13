package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "profile_access_token")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ChannelAccessTokenEntity extends BaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profileEntity;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "ip", nullable = false, unique = true)
    private String ip;

    @Column(name = "device_name", nullable = false, unique = true)
    private String deviceName;

    @Column(name = "additional_data", nullable = false, unique = true)
    private String additionalData;

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
