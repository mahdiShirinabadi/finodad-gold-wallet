package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "reverse_request")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
@Setter
@Getter
@ToString
public class ReverseRequestEntity extends RequestEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rrn_id", nullable = false)
	private RrnEntity rrnEntity;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "purchase_request_id", nullable = false)
	private PurchaseRequestEntity purchaseRequestEntity;
}
