package com.melli.wallet.service;

import java.util.Date;

/**
 * Service interface for person2person statistics operations
 */
public interface StatPerson2PersonService {

    /**
     * Generate person2person statistics for a given date range
     * @param fromDate Start date for statistics generation
     * @param toDate End date for statistics generation
     * @param creator Creator identifier for audit purposes
     */
    void generatePerson2PersonStatistics(Date fromDate, Date toDate, String creator);

    /**
     * Save person2person statistics entity
     * @param statPerson2PersonEntity Statistics entity to save
     * @param creator Creator identifier for audit purposes
     */
    void savePerson2PersonStatistics(com.melli.wallet.domain.master.entity.StatPerson2PersonEntity statPerson2PersonEntity, String creator);

    /**
     * Find the last person2person statistics record
     * @return Optional containing the last record if exists
     */
    java.util.Optional<com.melli.wallet.domain.master.entity.StatPerson2PersonEntity> findLastPerson2PersonStatisticsRecord();

    /**
     * Delete person2person statistics by Persian calculation date
     * @param persianDate Persian date string
     */
    void deletePerson2PersonStatisticsByDate(String persianDate);
}
