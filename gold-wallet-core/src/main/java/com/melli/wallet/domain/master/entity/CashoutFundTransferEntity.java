package com.melli.wallet.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "cash_out_fund_transfer")
@PrimaryKeyJoinColumn(name = "request_id", referencedColumnName = "id")
public class CashoutFundTransferEntity extends BaseEntityAudit implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cash_out_request_id", nullable = false)
    private RequestEntity cashoutRequestEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_transfer_account_to_account_request_id", nullable = false)
    private RequestEntity fundTransferAccountToAccountRequestEntity;

}
