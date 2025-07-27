package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@Entity
@Table(name = "resource")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ReportResourceEntity extends ReportBaseEntityAudit implements GrantedAuthority {

    @Column(name= "name")
    private String name;

    @Column(name= "fa_name")
    private String faName;

    @Column(name= "display")
    private int display;

    @ManyToMany(mappedBy = "resources")
    private Set<ReportRoleEntity> roles;

    @Override
    public String getAuthority() {
        return name;
    }
} 