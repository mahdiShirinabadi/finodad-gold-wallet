package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "setting_general")
public class SettingGeneralEntity extends BaseEntityAudit implements Serializable {

	@Column(name = "name")
	private String name;

	@Column(name = "value")
	private String value;

	@Column(name = "pattern")
	private String pattern;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "end_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

}
