package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "gift_card_payment_request")
@Setter
@Getter
@ToString
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class GiftCardPaymentRequestEntity extends RequestEntity {


	@Column(name = "quantity", precision = 10, scale = 5)
	private BigDecimal quantity; // quantity

	@ManyToOne
	@JoinColumn(name = "gift_card_id", nullable = false)
	private GiftCardEntity giftCardEntity;

	@ManyToOne
	@JoinColumn(name = "wallet_account_id", nullable = false)
	private WalletAccountEntity destinationAccountWalletEntity;

	@Column(name = "additional_data", length = 500)
	private String additionalData;
}
