package com.melli.wallet.domain.master.entity;

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
@Table(name = "wallet_account_type")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WalletAccountTypeEntity extends BaseEntityAudit implements Serializable {

	//WAGE
	//NORMAL
	//GIFT_CARD
	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "display", columnDefinition = "boolean default true")
	private Boolean display;
}
