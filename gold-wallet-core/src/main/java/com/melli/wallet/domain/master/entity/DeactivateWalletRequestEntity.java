package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

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
