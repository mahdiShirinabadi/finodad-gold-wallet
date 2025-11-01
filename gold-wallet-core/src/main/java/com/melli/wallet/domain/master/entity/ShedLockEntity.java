package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Setter
@Getter
@ToString
@Table(name = "shedlock")
public class ShedLockEntity {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "lock_until", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockUntil;

    @Column(name = "locked_at", updatable = false, insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockAt;

    @Column(name = "locked_by")
    private String lockedBy;
}
