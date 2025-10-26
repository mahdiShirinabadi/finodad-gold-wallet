package com.melli.wallet.service;

import java.util.Date;

/**
 * Service interface for buy statistics operations
 */
public interface StatBuyService {

    /**
     * Generate buy statistics for a given date range
     * @param fromDate Start date for statistics generation
     * @param toDate End date for statistics generation
     * @param creator Creator identifier for audit purposes
     */
    void generateBuyStatistics(Date fromDate, Date toDate, String creator);

    /**
     * Save buy statistics entity
     * @param statBuyEntity Statistics entity to save
     * @param creator Creator identifier for audit purposes
     */
    void saveBuyStatistics(com.melli.wallet.domain.master.entity.StatBuyEntity statBuyEntity, String creator);

    /**
     * Find the last buy statistics record
     * @return Optional containing the last record if exists
     */
    java.util.Optional<com.melli.wallet.domain.master.entity.StatBuyEntity> findLastBuyStatisticsRecord();

    /**
     * Delete buy statistics by Persian calculation date
     * @param persianDate Persian date string
     */
    void deleteBuyStatisticsByDate(String persianDate);
}
