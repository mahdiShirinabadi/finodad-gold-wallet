package com.melli.wallet.domain.master.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;



@Entity
@Table(name = "request_type")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RequestTypeEntity extends BaseEntityAudit {

    @Column(name= "name")
    private String name;

    @Column(name= "fa_name")
    private String faName;

    @Column(name= "display")
    private int display;
}
