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
@Table(name = "wallet_account_currency")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class WalletAccountCurrencyEntity extends BaseEntityAudit implements Serializable {

	//GOLD
	//RIAL
	//SILVER
	//PLATINUM
	@Column(name = "name")
	private String name;

	@Column(name = "suffix")
	private String suffix;
}
