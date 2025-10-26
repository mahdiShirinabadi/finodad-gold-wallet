package com.melli.wallet.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stat_physical_cash_out")
public class StatPhysicalCashOutEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "result")
    private String result;

    @Column(name = "count")
    private Long count;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "persian_calc_date")
    private String persianCalcDate;

    @Column(name = "georgian_calc_date")
    private Date georgianCalcDate;
}
