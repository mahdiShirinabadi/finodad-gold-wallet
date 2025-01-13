package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "verify_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
public class VerifyRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "purchase_request_id", nullable = false)
	private PurchaseRequestEntity purchaseRequestEntity;
}
