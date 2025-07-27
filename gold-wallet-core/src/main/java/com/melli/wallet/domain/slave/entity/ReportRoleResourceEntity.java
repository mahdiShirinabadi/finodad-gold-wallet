package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "role_resource")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportRoleResourceEntity extends ReportBaseEntityAudit {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private ReportRoleEntity roleEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private ReportResourceEntity resourceEntity;
} 