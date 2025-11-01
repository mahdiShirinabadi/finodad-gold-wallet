package com.melli.wallet.domain.slave.entity;

import com.melli.wallet.domain.master.entity.BaseEntityAudit;
import com.melli.wallet.domain.master.entity.RequestEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "cash_out_fund_transfer")
public class ReportCashoutFundTransferEntity extends ReportBaseEntityAudit implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cash_out_request_id", nullable = false)
    private ReportRequestEntity cashoutRequestEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_transfer_account_to_account_request_id", nullable = false)
    private ReportRequestEntity fundTransferAccountToAccountRequestEntity;

}
