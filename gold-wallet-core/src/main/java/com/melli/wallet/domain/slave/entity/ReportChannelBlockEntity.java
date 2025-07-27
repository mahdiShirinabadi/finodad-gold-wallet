package com.melli.wallet.domain.slave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "channel_block")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportChannelBlockEntity extends ReportBaseEntityAudit implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id", nullable = false)
    private ReportChannelEntity channelEntity;

    @Column(name = "start_block_date", nullable = false)
    private Date startBlockDate;

    @Column(name = "end_block_date")
    private Date endBlockDate;

    @Column(name = "count_fail", nullable = false)
    private int countFail;

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ReportChannelBlockEntity channelBlockEntity)) {
            return false;
        }

        return super.getId() == channelBlockEntity.getId();

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.getId());
    }
} 