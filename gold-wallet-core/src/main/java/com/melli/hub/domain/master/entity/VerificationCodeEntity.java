package com.melli.hub.domain.master.entity;

import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "verification_code")
public class VerificationCodeEntity extends BaseEntityAudit implements Serializable {

	@Column(name = "national_code", nullable = false)
	private String nationalCode;

	@Column(name = "code", nullable = false)
	private String code;

	@Column(name = "expire_time", nullable = false)
	private Date expireTime;

	@Column(name = "status")
	private int status;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private VerificationCodeEnum verificationCodeEnum;

}
