package com.melli.hub.domain.master.entity;

import com.melli.hub.domain.enumaration.RegisterProfileStepStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "temp_register_profile")
public class TempRegisterProfileEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "national_code", nullable = false, unique = true)
    private String nationalCode;

    @Column(name = "mobile", nullable = false, unique = true)
    private String mobile;

    @Column(name = "otp", nullable = false, unique = true)
    private String otp;

    @Column(name = "check_shahkar", nullable = false, unique = true)
    private String checkShahkar;

    @Column(name = "step")
    @Enumerated(EnumType.STRING)
    private RegisterProfileStepStatus step;

    @Column(name = "ip")
    private String ip;

    @Column(name = "temp_uuid")
    private String tempUuid;

    @Column(name = "expire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireTime;
}
