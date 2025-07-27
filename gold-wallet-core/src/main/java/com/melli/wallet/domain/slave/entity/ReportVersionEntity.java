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
@Table(name = "version")
public class ReportVersionEntity extends ReportBaseEntityAudit implements Serializable {

    @Column(name = "version_number", nullable = false)
    private String versionNumber;

    @Column(name = "changes", nullable = false)
    private String changes;

    @Column(name = "additional_data")
    private String additionalData;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
} 