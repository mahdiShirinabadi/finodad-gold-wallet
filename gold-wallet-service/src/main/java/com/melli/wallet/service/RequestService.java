package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import java.util.Date;
import java.util.List;

public interface RequestService {


    void save(RequestEntity requestEntity) throws InternalServiceException;

    public void save(RequestEntity requestEntity, List<MerchantEntity> merchantEntityList, WalletAccountTypeEntity walletAccountTypeEntity);

//    void save(RequestWageHistory requestWageHistory);

    void checkTraceIdIsUnique(long traceId, RequestEntity requestEntity) throws InternalServiceException;

    void findSuccessCashInByRefNumber(String refNumber) throws InternalServiceException;

    PurchaseRequestEntity findPurchaseRequestByRrnId(long traceId) throws InternalServiceException;


    PurchaseRequestEntity findPurchaseRequestById(long requestId);

    VerifyRequestEntity findVerifyRequestByRrnId(long traceId);

    ReverseRequestEntity findReverseRequestByRrnId(long traceId);

    List<Long> findReversiblePurchase(Integer[] purchaseChannelResult, Integer[] verifyChannelResult, Integer[] reverseChannelResult, Date fromReverseDate, Date toReverseDate);

//    PurchaseTrackRequest findPurchaseTrackRequest(long traceId);

    CashOutRequestEntity findCashOutRequest(long id);

    CashOutRequestEntity findCashOutRequestByRequestId(long requestId);

    List<PurchaseRequestEntity> findValidPurchase(MerchantEntity merchantEntity, Integer[] result, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity);

    List<PurchaseRequestEntity> findValidPurchaseByAccountType(Integer[] result, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity);

    void logHistory(RequestEntity request, String comment, String chargeDesc, int chargeResult);

    CashInRequestEntity findCashInWithId(long requestId);

    CashInRequestEntity findCashInWithRrnId(long rrnId) throws InternalServiceException;

    List<Long> findPurchaseIdsByTerminalId(String likeStr, Integer[] results, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity);

    CashInTrackResponse cashInTrack(String uid, String channelIp) throws InternalServiceException;

//    DechargeTrackResponse deChargeTrack(String uid, String channelIp) throws ServiceException;
//    DedicateDechargeRequest findDedicateDechargeByCashInRequestId(long requestId) throws ServiceException;

}
