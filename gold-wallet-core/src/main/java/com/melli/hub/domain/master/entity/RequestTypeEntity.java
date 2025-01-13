package com.melli.hub.domain.master.entity;



import jakarta.persistence.*;
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
