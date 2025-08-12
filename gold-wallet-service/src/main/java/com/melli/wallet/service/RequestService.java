package com.melli.wallet.service;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.slave.entity.ReportCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportPhysicalCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportPurchaseRequestEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Date;

public interface RequestService {

    void save(RequestEntity requestEntity) throws InternalServiceException;
    void checkTraceIdIsUnique(long traceId, RequestEntity requestEntity) throws InternalServiceException;
    void findSuccessCashInByRefNumber(String refNumber) throws InternalServiceException;
    ReportPurchaseRequestEntity findPurchaseRequestByRrnId(long traceId) throws InternalServiceException;
    CashInRequestEntity findCashInWithRrnId(long rrnId) throws InternalServiceException;
    ReportCashOutRequestEntity findCashOutWithRrnId(long rrnId) throws InternalServiceException;
    ReportPhysicalCashOutRequestEntity findPhysicalCashOutWithRrnId(long rrnId) throws InternalServiceException;
    void findCashInDuplicateWithRrnId(long rrnId) throws InternalServiceException;
    void findCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException;
    void findPhysicalCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException;
    AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDate(long[] walletAccountId, String transactionType, Date fromDate, Date toDate);
    AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDateByChannelId(long channelId, String transactionType, Date fromDate, Date toDate);
    AggregationPurchaseDTO findPhysicalByChannelAndDate(long channelId, Date fromDate, Date toDate);
    AggregationCashInDTO findSumAmountCashInBetweenDate(long[] walletAccountId, Date fromDate, Date toDate);
    AggregationCashOutDTO findSumAmountCashOutBetweenDate(long[] walletAccountId, Date fromDate, Date toDate);
    AggregationCashOutDTO findSumAmountPhysicalCashOutBetweenDate(long[] walletAccountId, Date fromDate, Date toDate);
}
