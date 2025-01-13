package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
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
