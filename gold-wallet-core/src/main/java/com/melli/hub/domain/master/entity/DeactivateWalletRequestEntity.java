package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "deactivate_wallet_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
public class DeactivateWalletRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_id", nullable = false)
	private WalletEntity  walletEntity;

}
