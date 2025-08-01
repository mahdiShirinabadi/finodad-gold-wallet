package com.melli.wallet.domain.slave.entity;

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
@Table(name = "wallet_level")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ReportWalletLevelEntity extends ReportBaseEntityAudit implements Serializable {

	//FOR BORONZ
	//FOR SILVER
	//FOR GOLD
	@Column(name = "name")
	private String name;
} 