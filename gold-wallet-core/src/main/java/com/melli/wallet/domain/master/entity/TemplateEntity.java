package com.melli.wallet.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "template")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TemplateEntity extends BaseEntityAudit implements Serializable  {

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

}
