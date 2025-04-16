package com.melli.wallet.domain.master.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "deactivate_wallet_account_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
public class DeactivateWalletAccountRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity  walletAccount;

}
