package com.melli.wallet.domain.master.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "create_wallet_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CreateWalletRequestEntity extends RequestEntity {

	@Column(name = "national_code", nullable = false)
	private String nationalCode;
}
