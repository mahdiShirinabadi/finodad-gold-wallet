package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
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
public class ResourceEntity extends BaseEntityAudit implements GrantedAuthority {


    @Column(name= "name")
    private String name;

    @Column(name= "fa_name")
    private String faName;

    @Column(name= "display")
    private int display;

    @ManyToMany(mappedBy = "resources")
    private Set<RoleEntity> roles;

    @Override
    public String getAuthority() {
        return name;
    }
}
