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
@Table(name = "stat_sell")
public class StatSellEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "result")
    private String result;

    @Column(name = "count")
    private Long count;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "price")
    private Long price;

    @Column(name = "persian_calc_date")
    private String persianCalcDate;

    @Column(name = "georgian_calc_date")
    private Date georgianCalcDate;
}
