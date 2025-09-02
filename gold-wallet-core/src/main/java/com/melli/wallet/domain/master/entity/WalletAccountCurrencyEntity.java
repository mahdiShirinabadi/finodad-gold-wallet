package com.melli.wallet.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.io.Serializable;

/**
 * Created by shirinabadi on 03/11/2016.
 *
 */
@Entity
@Table(name = "wallet_account_currency", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
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
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "standard_name", nullable = false)
	private String standardName;

	@Column(name = "suffix")
	private String suffix;

	@Column(name = "additional_data")
	private String additionalData;

	@Column(name = "description")
	private String description;
}
