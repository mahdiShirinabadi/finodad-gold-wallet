package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "profile_block")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileBlockEntity extends BaseEntityAudit implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profileEntity;

    @Column(name = "start_block_date", nullable = false)
    private Date startBlockDate;

    @Column(name = "end_block_date")
    private Date endBlockDate;

    @Column(name = "count_fail", nullable = false)
    private int countFail;


    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ProfileBlockEntity profileBlockEntity)) {
            return false;
        }

        return super.getId() == profileBlockEntity.getId();

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.getId());
    }
}
