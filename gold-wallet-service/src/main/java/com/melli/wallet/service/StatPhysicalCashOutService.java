package com.melli.wallet.service;

import java.util.Date;

/**
 * Service interface for physical cash out statistics operations
 */
public interface StatPhysicalCashOutService {

    /**
     * Generate physical cash out statistics for a given date range
     * @param fromDate Start date for statistics generation
     * @param toDate End date for statistics generation
     * @param creator Creator identifier for audit purposes
     */
    void generatePhysicalCashOutStatistics(Date fromDate, Date toDate, String creator);

    /**
     * Save physical cash out statistics entity
     * @param statPhysicalCashOutEntity Statistics entity to save
     * @param creator Creator identifier for audit purposes
     */
    void savePhysicalCashOutStatistics(com.melli.wallet.domain.master.entity.StatPhysicalCashOutEntity statPhysicalCashOutEntity, String creator);

    /**
     * Find the last physical cash out statistics record
     * @return Optional containing the last record if exists
     */
    java.util.Optional<com.melli.wallet.domain.master.entity.StatPhysicalCashOutEntity> findLastPhysicalCashOutStatisticsRecord();

    /**
     * Delete physical cash out statistics by Persian calculation date
     * @param persianDate Persian date string
     */
    void deletePhysicalCashOutStatisticsByDate(String persianDate);
}
