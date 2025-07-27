package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "channel_role")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportChannelRoleEntity extends ReportBaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private ReportRoleEntity roleEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id", nullable = false)
    private ReportChannelEntity channelEntity;
} 