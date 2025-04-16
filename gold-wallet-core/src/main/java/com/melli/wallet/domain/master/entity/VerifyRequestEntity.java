package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "verify_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
public class VerifyRequestEntity extends RequestEntity {

	@ManyToOne
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;


	@ManyToOne
	@JoinColumn(name = "purchase_request_id", nullable = false)
	private PurchaseRequestEntity purchaseRequestEntity;
}
