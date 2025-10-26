package com.melli.wallet.service;

import java.util.Date;

/**
 * Service interface for sell statistics operations
 */
public interface StatSellService {

    /**
     * Generate sell statistics for a given date range
     * @param fromDate Start date for statistics generation
     * @param toDate End date for statistics generation
     * @param creator Creator identifier for audit purposes
     */
    void generateSellStatistics(Date fromDate, Date toDate, String creator);

    /**
     * Save sell statistics entity
     * @param statSellEntity Statistics entity to save
     * @param creator Creator identifier for audit purposes
     */
    void saveSellStatistics(com.melli.wallet.domain.master.entity.StatSellEntity statSellEntity, String creator);

    /**
     * Find the last sell statistics record
     * @return Optional containing the last record if exists
     */
    java.util.Optional<com.melli.wallet.domain.master.entity.StatSellEntity> findLastSellStatisticsRecord();

    /**
     * Delete sell statistics by Persian calculation date
     * @param persianDate Persian date string
     */
    void deleteSellStatisticsByDate(String persianDate);
}
