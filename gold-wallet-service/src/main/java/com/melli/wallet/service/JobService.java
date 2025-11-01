package com.melli.wallet.service;

import com.melli.wallet.exception.InternalServiceException;

/**
 * Service interface for wallet statistics job operations
 */
public interface JobService {

    /**
     * Generate buy statistics for a given date range
     * @param fromDate Start date for statistics generation (Persian date format)
     * @param toDate End date for statistics generation (Persian date format)
     * @param creator Creator identifier for audit purposes
     */
    void generateBuyStatistics(String fromDate, String toDate, String creator) throws InternalServiceException;

    /**
     * Generate sell statistics for a given date range
     * @param fromDate Start date for statistics generation (Persian date format)
     * @param toDate End date for statistics generation (Persian date format)
     * @param creator Creator identifier for audit purposes
     */
    void generateSellStatistics(String fromDate, String toDate, String creator) throws InternalServiceException;

    /**
     * Generate wallet statistics for a given date range
     * @param fromDate Start date for statistics generation (Persian date format)
     * @param toDate End date for statistics generation (Persian date format)
     * @param creator Creator identifier for audit purposes
     */
    void generateWalletStatistics(String fromDate, String toDate, String creator) throws InternalServiceException;

    /**
     * Generate person2person statistics for a given date range
     * @param fromDate Start date for statistics generation (Persian date format)
     * @param toDate End date for statistics generation (Persian date format)
     * @param creator Creator identifier for audit purposes
     */
    void generatePerson2PersonStatistics(String fromDate, String toDate, String creator) throws InternalServiceException;

    /**
     * Generate physical cash out statistics for a given date range
     * @param fromDate Start date for statistics generation (Persian date format)
     * @param toDate End date for statistics generation (Persian date format)
     * @param creator Creator identifier for audit purposes
     */
    void generatePhysicalCashOutStatistics(String fromDate, String toDate, String creator) throws InternalServiceException;

    void batchSettlement() throws InternalServiceException;
}
