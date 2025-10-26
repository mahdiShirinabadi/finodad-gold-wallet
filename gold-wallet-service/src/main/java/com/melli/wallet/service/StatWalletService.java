package com.melli.wallet.service;

import java.util.Date;

/**
 * Service interface for wallet statistics operations
 */
public interface StatWalletService {

    /**
     * Generate wallet statistics for a given date range
     * @param fromDate Start date for statistics generation
     * @param toDate End date for statistics generation
     * @param creator Creator identifier for audit purposes
     */
    void generateWalletStatistics(Date fromDate, Date toDate, String creator);

    /**
     * Save wallet statistics entity
     * @param statWalletEntity Statistics entity to save
     * @param creator Creator identifier for audit purposes
     */
    void saveWalletStatistics(com.melli.wallet.domain.master.entity.StatWalletEntity statWalletEntity, String creator);

    /**
     * Find the last wallet statistics record
     * @return Optional containing the last record if exists
     */
    java.util.Optional<com.melli.wallet.domain.master.entity.StatWalletEntity> findLastWalletStatisticsRecord();

    /**
     * Delete wallet statistics by Persian calculation date
     * @param persianDate Persian date string
     */
    void deleteWalletStatisticsByDate(String persianDate);
}
