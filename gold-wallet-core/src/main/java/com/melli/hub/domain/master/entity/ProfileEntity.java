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
@Table(name = "profile")
public class ProfileEntity extends BaseEntityAudit implements Serializable, UserDetails {


    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "national_code", nullable = false, unique = true)
    private String nationalCode;

    @Column(name = "mobile", nullable = false, unique = true)
    private String mobile;

    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "birth_date", nullable = false, unique = true)
    private String birthDate;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "two_factor_authentication", nullable = false, unique = true)
    private Boolean towFactorAuthentication;

    @Column(name = "status")
    private String status;

    @Column(name = "valid_ip")
    private String validIp;

    @Column(name = "level")
    private String level;

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
