package com.melli.hub.domain.master.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class Name: ProviderEntity
 * Author: Mahdi Shirinabadi
 * Date: 1/12/2025
 */
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "provider")
public class ProviderEntity extends BaseEntityAudit implements Serializable, UserDetails {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "national_code", nullable = false, unique = true)
    private String nationalCode;

    @Column(name = "mobile", nullable = false, unique = true)
    private String mobile;

    @Column(name = "economical_code", nullable = false, unique = true)
    private String economicalCode;

    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "valid_ip")
    private String validIp;

    @Column(name = "price_gold_sell")
    private String priceGoldSell;

    @Column(name = "price_gold_buy")
    private String priceGoldBuy;

    @Column(name = "commission_gold_sell")
    private String commissionGoldSell;

    @Column(name = "commission_gold_buy")
    private String commissionGoldBuy;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "profileEntity")
    private List<ProfileRoleEntity> profileRoleList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return profileRoleList.stream().flatMap(role->role.getRoleEntity().getResources().stream()).map(resourceEntity -> new SimpleGrantedAuthority(resourceEntity.getName())).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return nationalCode;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
