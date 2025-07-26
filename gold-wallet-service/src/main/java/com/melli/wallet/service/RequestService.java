package com.melli.wallet.service;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RequestService {

    void save(RequestEntity requestEntity) throws InternalServiceException;
    void checkTraceIdIsUnique(long traceId, RequestEntity requestEntity) throws InternalServiceException;
    void findSuccessCashInByRefNumber(String refNumber) throws InternalServiceException;
    PurchaseRequestEntity findPurchaseRequestByRrnId(long traceId) throws InternalServiceException;
    PurchaseRequestEntity findPurchaseRequestById(long requestId);
    CashOutRequestEntity findCashOutRequest(long id);
    CashOutRequestEntity findCashOutRequestByRequestId(long requestId);
    List<PurchaseRequestEntity> findValidPurchase(MerchantEntity merchantEntity, Integer[] result, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity);
    List<PurchaseRequestEntity> findValidPurchaseByAccountType(Integer[] result, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity);
    void logHistory(RequestEntity request, String comment, String chargeDesc, int chargeResult);
    CashInRequestEntity findCashInWithId(long requestId);
    CashInRequestEntity findCashInWithRrnId(long rrnId) throws InternalServiceException;
    CashOutRequestEntity findCashOutWithRrnId(long rrnId) throws InternalServiceException;
    PhysicalCashOutRequestEntity findPhysicalCashOutWithRrnId(long rrnId) throws InternalServiceException;
    void findCashInDuplicateWithRrnId(long rrnId) throws InternalServiceException;
    void findCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException;
    void findPhyicalCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException;
    List<Long> findPurchaseIdsByTerminalId(String likeStr, Integer[] results, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity);
    CashInTrackResponse cashInTrack(String uid, String channelIp) throws InternalServiceException;
    AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDate(@Param("walletAccountId") long[] walletAccountId, String transactionType, Date fromDate, Date toDate);
    AggregationCashInDTO findSumAmountCashInBetweenDate(@Param("walletAccountId") long[] walletAccountId, Date fromDate, Date toDate);
    AggregationCashOutDTO findSumAmountCashOutBetweenDate(@Param("walletAccountId") long[] walletAccountId, Date fromDate, Date toDate);
    AggregationCashOutDTO findSumAmountPhysicalCashOutBetweenDate(@Param("walletAccountId") long[] walletAccountId, Date fromDate, Date toDate);
}
