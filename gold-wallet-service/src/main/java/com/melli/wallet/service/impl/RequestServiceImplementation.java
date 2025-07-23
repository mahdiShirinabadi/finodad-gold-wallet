package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.CashInRequestRepository;
import com.melli.wallet.domain.master.persistence.CashOutRequestRepository;
import com.melli.wallet.domain.master.persistence.PurchaseRequestRepository;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.RequestService;
import com.melli.wallet.service.RrnService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Class Name: RequestServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RequestServiceImplementation implements RequestService {


    private final CashInRequestRepository cashInRequestRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final RrnService rrnService;
    private final Helper helper;
    private final StatusService statusService;
    private final CashOutRequestRepository cashOutRequestRepository;


    @Override
    public void save(RequestEntity requestEntity) throws InternalServiceException {
        log.info("start save object request ===> {}", requestEntity);
        if (requestEntity instanceof CashInRequestEntity cashInRequestEntity) {
            cashInRequestRepository.save(cashInRequestEntity);
        } else if (requestEntity instanceof PurchaseRequestEntity purchaseRequestEntity) {
            purchaseRequestRepository.save(purchaseRequestEntity);
        }else if (requestEntity instanceof CashOutRequestEntity cashOutRequestEntity) {
            cashOutRequestRepository.save(cashOutRequestEntity);
        } else {
            log.error("requestEntity is not instanceof");
            throw new InternalServiceException("error in save request, instance not define", StatusService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public void checkTraceIdIsUnique(long traceId, RequestEntity requestEntity) throws InternalServiceException {

        log.info("start find request with traceId ===> {}", traceId);

        RequestEntity resultRequest = new RequestEntity();

        if (requestEntity instanceof CashInRequestEntity) {
            resultRequest = cashInRequestRepository.findByRrnEntityId(traceId);
        } else if (requestEntity instanceof PurchaseRequestEntity) {
            resultRequest = purchaseRequestRepository.findByRrnEntityId(traceId);
        }

        if (resultRequest != null) {
            log.error("traceId ({}) is not unique", traceId);
            throw new InternalServiceException("traceId ( " + traceId + ") is not unique", StatusService.DUPLICATE_UUID, HttpStatus.OK);
        }

        log.info("finish find request with traceId ===> {}", traceId);
    }

    @Override
    public void findSuccessCashInByRefNumber(String refNumber) throws InternalServiceException {
        CashInRequestEntity cashInRequest = cashInRequestRepository.findByRefNumber(refNumber);
        if (cashInRequest != null) {
            log.error("refNumber ({}), used before !!! ", refNumber);
            throw new InternalServiceException("cashIn: refNumber (" + refNumber + ") used before !!!", StatusService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
        }
    }

    @Override
    public PurchaseRequestEntity findPurchaseRequestByRrnId(long traceId) throws InternalServiceException {
        return purchaseRequestRepository.findByRrnEntityId(traceId);
    }

    @Override
    public PurchaseRequestEntity findPurchaseRequestById(long requestId) {
        return purchaseRequestRepository.findById(requestId);
    }


    @Override
    public CashOutRequestEntity findCashOutRequest(long id) {
        return null;
    }

    @Override
    public CashOutRequestEntity findCashOutRequestByRequestId(long requestId) {
        return null;
    }

    @Override
    public List<PurchaseRequestEntity> findValidPurchase(MerchantEntity merchantEntity, Integer[] result, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity) {
        return List.of();
    }

    @Override
    public List<PurchaseRequestEntity> findValidPurchaseByAccountType(Integer[] result, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity) {
        return List.of();
    }

    @Override
    public void logHistory(RequestEntity request, String comment, String chargeDesc, int chargeResult) {

    }

    @Override
    public CashInRequestEntity findCashInWithId(long requestId) {
        return cashInRequestRepository.findById(requestId);
    }

    @Override
    public CashInRequestEntity findCashInWithRrnId(long rrnId) throws InternalServiceException {
        return cashInRequestRepository.findOptionalByRrnEntityId(rrnId).orElseThrow(() -> {
            log.error("cashInRequest with id ({}) not found", rrnId);
            return new InternalServiceException("cashIn not found", StatusService.RECORD_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public CashOutRequestEntity findCashOutWithRrnId(long rrnId) throws InternalServiceException {
        return cashOutRequestRepository.findOptionalByRrnEntityId(rrnId).orElseThrow(() -> {
            log.error("cashInRequest with id ({}) not found", rrnId);
            return new InternalServiceException("cashIn not found", StatusService.RECORD_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public void findCashInDuplicateWithRrnId(long rrnId) throws InternalServiceException {
       CashInRequestEntity cashInRequestEntity = cashInRequestRepository.findByRrnEntityId(rrnId);
       if(cashInRequestEntity != null) {
           log.error("cashInDuplicateWithRrnId ({}) found", rrnId);
           throw new InternalServiceException("cashInDuplicateWithRrnId", StatusService.DUPLICATE_UUID, HttpStatus.OK);
       }
    }

    @Override
    public void findCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException {
        CashOutRequestEntity cashOutRequestEntity = cashOutRequestRepository.findByRrnEntityId(rrnId);
        if(cashOutRequestEntity != null) {
            log.error("cashOutDuplicateWithRrnId ({}) found", rrnId);
            throw new InternalServiceException("cashInDuplicateWithRrnId", StatusService.DUPLICATE_UUID, HttpStatus.OK);
        }
    }

    @Override
    public List<Long> findPurchaseIdsByTerminalId(String likeStr, Integer[] results, Date fromDate, Date toDate, WalletAccountTypeEntity walletAccountTypeEntity) {
        return List.of();
    }

    @Override
    public CashInTrackResponse cashInTrack(String uid, String channelIp) throws InternalServiceException {
        log.info("star to cashInTrack with uid ( {} ) and channelIp ( {} )", uid, channelIp);
        RrnEntity rrnEntity = rrnService.findByUid(uid);
        if (rrnEntity == null) {
            log.error("rrn with uid ( {} ) not found", uid);
            throw new InternalServiceException("can not find rrn with uid ( " + uid + " )", StatusService.UUID_NOT_FOUND, HttpStatus.OK);
        }
        log.info("start tracking cashIn  with traceId ( {} ) ...", rrnEntity.getUuid());
        CashInRequestEntity cashInRequest = cashInRequestRepository.findByRrnEntity(rrnEntity);
        return helper.fillCashInTrackResponse(cashInRequest, statusService);
    }

    @Override
    public AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDate(long[] walletAccountId, String transactionType, Date fromDate, Date toDate) {
        return purchaseRequestRepository.findSumAmountByTransactionTypeBetweenDate(walletAccountId, transactionType, fromDate, toDate);
    }

    @Override
    public AggregationCashInDTO findSumAmountCashInBetweenDate(long[] walletAccountId, Date fromDate, Date toDate) {
        return cashInRequestRepository.findSumAmountBetweenDate(walletAccountId, fromDate, toDate);
    }

    @Override
    public AggregationCashOutDTO findSumAmountCashOutBetweenDate(long[] walletAccountId, Date fromDate, Date toDate) {
        return cashOutRequestRepository.findSumAmountBetweenDate(walletAccountId, fromDate, toDate);
    }
}
