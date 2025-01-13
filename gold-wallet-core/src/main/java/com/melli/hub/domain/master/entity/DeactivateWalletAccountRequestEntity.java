package com.melli.hub.domain.master.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

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
