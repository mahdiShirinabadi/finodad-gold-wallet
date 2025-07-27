package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "create_wallet_account_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CreateWalletAccountRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity walletAccountEntity;

	@Column(name = "send_message")
	private String sendMessage;

	@Column(name = "send_message_date")
	private Date sendMessageDate;

	@Column(name = "receive_message")
	private String receiveMessage;

	@Column(name = "receive_date")
	private Date receiveDate;

}
