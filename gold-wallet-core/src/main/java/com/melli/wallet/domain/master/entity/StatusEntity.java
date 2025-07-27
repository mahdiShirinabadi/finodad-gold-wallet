package com.melli.wallet.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "status")
public class StatusEntity extends BaseEntityAudit implements Serializable {


    @Column(name = "code")
    private String code;

    @Column(name = "persian_description")
    private String persianDescription;

    @Column(name = "additional_data")
    private String additionalData;

}
