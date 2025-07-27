package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "role_")
public class ReportRoleEntity extends ReportBaseEntityAudit implements Serializable {

    @Column(name = "name")
    private String name;

    @Column(name = "persian_description")
    private String persianDescription;

    @Column(name = "additional_data")
    private String additionalData;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_resource",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private Set<ReportResourceEntity> resources;
} 