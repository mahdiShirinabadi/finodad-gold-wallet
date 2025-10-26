package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.slave.entity.ReportPurchaseRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportRrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ReportPurchaseRequestRepository extends CrudRepository<ReportPurchaseRequestEntity, Long> {

    ReportPurchaseRequestEntity findByRrnEntityId(long traceId);
    Optional<ReportPurchaseRequestEntity> findOptionalByRrnEntityId(long traceId);
    ReportPurchaseRequestEntity findByRrnEntity(ReportRrnEntity rrn);
    ReportPurchaseRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.price), 0) as sumPrice, count(*) as countRecord, COALESCE(SUM(p.quantity), 0) as sumQuantity from {h-schema}purchase_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0 and p.transaction_type = :transactionType", nativeQuery = true)
    AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("transactionType") String transactionType, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query(value = "select COALESCE(SUM(p.price), 0) as sumPrice, count(*) as countRecord, COALESCE(SUM(p.quantity), 0) as sumQuantity from {h-schema}purchase_request p inner join {h-schema}request r on p.request_id = r.id  where r.channel_id = :channelId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0 and p.transaction_type = :transactionType", nativeQuery = true)
    AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDateByChannel(@Param("channelId") long channelId, @Param("transactionType") String transactionType, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    /**
     * Find purchase statistics aggregated by channel, currency, merchant, and result for a specific date
     * This query reads from slave database for statistics generation (heavy transaction)
     */
    @Query(value = "SELECT " +
            "r.channel_id as channelId, " +
            "wa.currency_id as currencyId, " +
            "p.merchant_id as merchantId, " +
            "r.result as result, " +
            "COUNT(*) as count, " +
            "COALESCE(SUM(p.price), 0) as amount, " +
            "COALESCE(AVG(p.price), 0) as price, " +
            "DATE(r.created_at) as createDateAt " +
            "FROM {h-schema}purchase_request p " +
            "INNER JOIN {h-schema}request r ON p.request_id = r.id " +
            "INNER JOIN {h-schema}wallet_account wa ON p.wallet_account_id = wa.id " +
            "WHERE DATE(r.created_at) = DATE(:targetDate) " +
            "GROUP BY r.channel_id, wa.currency_id, p.merchant_id, r.result, DATE(r.created_at)", nativeQuery = true)
    java.util.List<PurchaseStatPerDay> findPurchaseAggregationPerDay(@Param("targetDate") Date targetDate);

    /**
     * Interface for purchase statistics per day result
     */
    interface PurchaseStatPerDay {
        Long getChannelId();
        Long getCurrencyId();
        Long getMerchantId();
        Integer getResult();
        Long getCount();
        Long getAmount();
        Long getPrice();
        String getCreateDateAt();
    }

} 