package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportSellCollateralRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ReportSellCollateralRequestRepository extends CrudRepository<ReportSellCollateralRequestEntity, Long> {

    /**
     * Find sell collateral statistics aggregated by channel, currency, merchant, and result for a specific date
     * This query reads from slave database for statistics generation (heavy transaction)
     */
    @Query(value = "SELECT " +
            "r.channel_id as channelId, " +
            "wa.currency_id as currencyId, " +
            "scr.merchant_id as merchantId, " +
            "r.result as result, " +
            "COUNT(*) as count, " +
            "COALESCE(SUM(scr.price), 0) as amount, " +
            "COALESCE(AVG(scr.price), 0) as price, " +
            "DATE(r.created_at) as createDateAt " +
            "FROM {h-schema}sell_collateral_request scr " +
            "INNER JOIN {h-schema}request r ON scr.request_id = r.id " +
            "INNER JOIN {h-schema}wallet_account wa ON scr.wallet_account_id = wa.id " +
            "WHERE DATE(r.created_at) = DATE(:targetDate) " +
            "GROUP BY r.channel_id, wa.currency_id, scr.merchant_id, r.result, DATE(r.created_at)", nativeQuery = true)
    java.util.List<SellStatPerDay> findSellAggregationPerDay(@Param("targetDate") Date targetDate);

    /**
     * Interface for sell statistics per day result
     */
    interface SellStatPerDay {
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
