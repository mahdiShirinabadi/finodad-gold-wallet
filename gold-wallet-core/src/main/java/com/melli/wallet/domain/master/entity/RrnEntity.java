package com.melli.wallet.domain.master.entity;


import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * Created by shirinabadi on 03/11/2016.
 *
 */
@Entity
@Table(name = "rrn")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RrnEntity extends BaseEntityAudit implements Serializable {

	@Column(name = "national_code")
	private String nationalCode;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "channel_id", nullable = false)
	private ChannelEntity channel;

	@Column(name = "uuid", insertable = false, updatable = false)
	private String uuid;
}
