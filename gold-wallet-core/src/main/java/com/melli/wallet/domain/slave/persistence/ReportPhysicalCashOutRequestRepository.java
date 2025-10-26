package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.dto.AggregationPhysicalCashOutDTO;
import com.melli.wallet.domain.slave.entity.ReportPhysicalCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportRrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportPhysicalCashOutRequestRepository extends CrudRepository<ReportPhysicalCashOutRequestEntity, Long> {

    ReportPhysicalCashOutRequestEntity findByRrnEntityId(long traceId);
    Optional<ReportPhysicalCashOutRequestEntity> findOptionalByRrnEntityId(long traceId);
    ReportPhysicalCashOutRequestEntity findByRrnEntity(ReportRrnEntity rrn);
    ReportPhysicalCashOutRequestEntity findById(long requestId);
    List<ReportPhysicalCashOutRequestEntity> findByWalletAccountEntity_IdIn(List<Long> walletAccountEntityIds);

    @Query(value = "select COALESCE(SUM(p.final_quantity), 0) as sumQuantity, count(*) as countRecord from {h-schema}physical_cash_out_request p inner join {h-schema}request r on p.request_id = r.id  where r.channel_id = :channelId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationPhysicalCashOutDTO findSumQuantityBetweenDate(@Param("channelId") long channelId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query(value = "select COALESCE(SUM(p.final_quantity), 0) as sumQuantity, count(*) as countRecord from {h-schema}physical_cash_out_request p inner join {h-schema}request r on p.request_id = r.id  where r.channel_id = :channelId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0 and p.wallet_account_id in :walletAccountId", nativeQuery = true)
    AggregationPhysicalCashOutDTO findSumQuantityBetweenDateByDetail(@Param("channelId") long channelId, @Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    /**
     * Find physical cash out statistics aggregated by channel, currency, and result for a specific date
     * This query reads from slave database for statistics generation (heavy transaction)
     */
    @Query(value = "SELECT " +
            "r.channel_id as channelId, " +
            "wa.currency_id as currencyId, " +
            "r.result as result, " +
            "COUNT(*) as count, " +
            "COALESCE(SUM(pco.quantity), 0) as amount, " +
            "DATE(r.created_at) as createDateAt " +
            "FROM {h-schema}physical_cash_out_request pco " +
            "INNER JOIN {h-schema}request r ON pco.request_id = r.id " +
            "INNER JOIN {h-schema}wallet_account wa ON pco.wallet_account_id = wa.id " +
            "WHERE DATE(r.created_at) = DATE(:targetDate) " +
            "GROUP BY r.channel_id, wa.currency_id, r.result, DATE(r.created_at)", nativeQuery = true)
    java.util.List<PhysicalCashOutStatPerDay> findPhysicalCashOutAggregationPerDay(@Param("targetDate") Date targetDate);

    /**
     * Interface for physical cash out statistics per day result
     */
    interface PhysicalCashOutStatPerDay {
        Long getChannelId();
        Long getCurrencyId();
        Integer getResult();
        Long getCount();
        Long getAmount();
        String getCreateDateAt();
    }
} 