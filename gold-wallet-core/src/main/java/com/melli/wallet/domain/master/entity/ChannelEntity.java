package com.melli.wallet.domain.master.entity;

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
 * Class Name: ProfileEntity
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "channel")
public class ChannelEntity extends BaseEntityAudit implements Serializable, UserDetails {


    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "mobile", nullable = false, unique = true)
    private String mobile;

    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "trust")
    private int trust;

    @Column(name = "sign")
    private int sign;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "ip")
    private String ip;

    @Column(name = "status")
    private String status;

    @Column(name = "iban")
    private String iban;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_id", nullable = true)
    private WalletEntity walletEntity;

    @Column(name = "account")
    private String account;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "check_shahkar")
    private int checkShahkar;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "channelEntity")
    private List<ChannelRoleEntity> channelRoleList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return channelRoleList.stream().flatMap(role->role.getRoleEntity().getResources().stream()).map(resourceEntity -> new SimpleGrantedAuthority(resourceEntity.getName())).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
