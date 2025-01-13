package com.melli.hub.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

/**
 * Created by shirinabadi on 03/11/2016.
 *
 */
@Entity
@Table(name = "wallet_type")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WalletTypeEntity extends BaseEntityAudit implements Serializable {

	//FOR CHANNEL
	//FOR MERCHANT
	//FOR NORMAL USER
	//

	@Column(name = "name")
	private String name;

	@Column(name = "code")
	private String code;

	@Column(name = "display")
	private int display;
}
