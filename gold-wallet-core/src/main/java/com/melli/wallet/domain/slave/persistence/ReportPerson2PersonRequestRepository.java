package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportPerson2PersonRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ReportPerson2PersonRequestRepository extends CrudRepository<ReportPerson2PersonRequestEntity, Long> {

    /**
     * Find person2person statistics aggregated by channel, currency, and result for a specific date
     * This query reads from slave database for statistics generation (heavy transaction)
     */
    @Query(value = "SELECT " +
            "r.channel_id as channelId, " +
            "wa.currency_id as currencyId, " +
            "r.result as result, " +
            "COUNT(*) as count, " +
            "COALESCE(SUM(p2p.amount), 0) as amount, " +
            "DATE(r.created_at) as createDateAt " +
            "FROM {h-schema}p_2_p_request p2p " +
            "INNER JOIN {h-schema}request r ON p2p.request_id = r.id " +
            "INNER JOIN {h-schema}wallet_account wa ON p2p.src_wallet_account_id = wa.id " +
            "WHERE DATE(r.created_at) = DATE(:targetDate) " +
            "GROUP BY r.channel_id, wa.currency_id, r.result, DATE(r.created_at)", nativeQuery = true)
    java.util.List<Person2PersonStatPerDay> findPerson2PersonAggregationPerDay(@Param("targetDate") Date targetDate);

    /**
     * Interface for person2person statistics per day result
     */
    interface Person2PersonStatPerDay {
        Long getChannelId();
        Long getCurrencyId();
        Integer getResult();
        Long getCount();
        Long getAmount();
        String getCreateDateAt();
    }
}
