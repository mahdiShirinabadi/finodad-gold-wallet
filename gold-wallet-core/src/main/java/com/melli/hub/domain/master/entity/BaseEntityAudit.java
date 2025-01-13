package com.melli.hub.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntityAudit extends BaseEntity implements Serializable {

    @Column(name = "created_by", updatable = false, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", insertable = false)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
