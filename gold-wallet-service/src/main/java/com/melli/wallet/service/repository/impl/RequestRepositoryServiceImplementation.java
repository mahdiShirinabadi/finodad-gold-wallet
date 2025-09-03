package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.dto.AggregationP2PDTO;
import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.*;
import com.melli.wallet.domain.slave.entity.ReportCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportPhysicalCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportPurchaseRequestEntity;
import com.melli.wallet.domain.slave.persistence.ReportCashOutRequestRepository;
import com.melli.wallet.domain.slave.persistence.ReportPhysicalCashOutRequestRepository;
import com.melli.wallet.domain.slave.persistence.ReportPurchaseRequestRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.RequestRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Class Name: RequestServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RequestRepositoryServiceImplementation implements RequestRepositoryService {


    private final CashInRequestRepository cashInRequestRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final ReportPurchaseRequestRepository reportPurchaseRequestRepository;
    private final ReportPhysicalCashOutRequestRepository reportPhysicalCashOutRequestRepository;
    private final CashOutRequestRepository cashOutRequestRepository;
    private final ReportCashOutRequestRepository reportCashOutRequestRepository;
    private final PhysicalCashOutRequestRepository physicalCashOutRequestRepository;
    private final P2PRequestRepository p2PRequestRepository;


    @Override
    public void save(RequestEntity requestEntity) throws InternalServiceException {
        log.info("start save object request ===> {}", requestEntity);
        switch (requestEntity) {
            case CashInRequestEntity cashInRequestEntity -> cashInRequestRepository.save(cashInRequestEntity);
            case PurchaseRequestEntity purchaseRequestEntity -> purchaseRequestRepository.save(purchaseRequestEntity);
            case CashOutRequestEntity cashOutRequestEntity -> cashOutRequestRepository.save(cashOutRequestEntity);
            case PhysicalCashOutRequestEntity physicalCashOutRequestEntity ->
                    physicalCashOutRequestRepository.save(physicalCashOutRequestEntity);
            case null, default -> {
                log.error("requestEntity is not instanceof");
                throw new InternalServiceException("error in save request, instance not define", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
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
        }else if (requestEntity instanceof CashOutRequestEntity) {
            resultRequest = cashOutRequestRepository.findByRrnEntityId(traceId);
        }else if (requestEntity instanceof PhysicalCashOutRequestEntity) {
            resultRequest = physicalCashOutRequestRepository.findByRrnEntityId(traceId);
        }else if (requestEntity instanceof Person2PersonRequestEntity) {
            resultRequest = p2PRequestRepository.findByRrnEntityId(traceId);
        }

        if (resultRequest != null) {
            log.error("traceId ({}) is not unique", traceId);
            throw new InternalServiceException("traceId ( " + traceId + ") is not unique", StatusRepositoryService.DUPLICATE_UUID, HttpStatus.OK);
        }

        log.info("finish find request with traceId ===> {}", traceId);
    }

    @Override
    public void findSuccessCashInByRefNumber(String refNumber) throws InternalServiceException {
        CashInRequestEntity cashInRequest = cashInRequestRepository.findByRefNumber(refNumber);
        if (cashInRequest != null) {
            log.error("refNumber ({}), used before !!! ", refNumber);
            throw new InternalServiceException("cashIn: refNumber (" + refNumber + ") used before !!!", StatusRepositoryService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
        }
    }

    @Override
    public ReportPurchaseRequestEntity findPurchaseRequestByRrnId(long traceId) throws InternalServiceException {
        return reportPurchaseRequestRepository.findByRrnEntityId(traceId);
    }

    @Override
    public CashInRequestEntity findCashInWithRrnId(long rrnId) throws InternalServiceException {
        return cashInRequestRepository.findOptionalByRrnEntityId(rrnId).orElseThrow(() -> {
            log.error("cashInRequest with id ({}) not found", rrnId);
            return new InternalServiceException("cashIn not found", StatusRepositoryService.RECORD_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public Person2PersonRequestEntity findP2pWithRrnId(long rrnId) throws InternalServiceException {
        return p2PRequestRepository.findOptionalByRrnEntityId(rrnId).orElseThrow(() -> {
            log.error("findP2pWithRrnId with id ({}) not found", rrnId);
            return new InternalServiceException("findP2pWithRrnId not found", StatusRepositoryService.RECORD_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public ReportCashOutRequestEntity findCashOutWithRrnId(long rrnId) throws InternalServiceException {
        return reportCashOutRequestRepository.findOptionalByRrnEntityId(rrnId).orElseThrow(() -> {
            log.error("cashOutRequest with id ({}) not found", rrnId);
            return new InternalServiceException("cashOutRequest not found", StatusRepositoryService.RECORD_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public ReportPhysicalCashOutRequestEntity findPhysicalCashOutWithRrnId(long rrnId) throws InternalServiceException {
        return reportPhysicalCashOutRequestRepository.findOptionalByRrnEntityId(rrnId).orElseThrow(() -> {
            log.error("physicalCashOutRequest with id ({}) not found", rrnId);
            return new InternalServiceException("physicalCashOutRequest not found", StatusRepositoryService.RECORD_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public void findCashInDuplicateWithRrnId(long rrnId) throws InternalServiceException {
       CashInRequestEntity cashInRequestEntity = cashInRequestRepository.findByRrnEntityId(rrnId);
       if(cashInRequestEntity != null) {
           log.error("cashInDuplicateWithRrnId ({}) found", rrnId);
           throw new InternalServiceException("cashInDuplicateWithRrnId", StatusRepositoryService.DUPLICATE_UUID, HttpStatus.OK);
       }
    }

    @Override
    public void findP2pDuplicateWithRrnId(long rrnId) throws InternalServiceException {
        Person2PersonRequestEntity requestEntity = p2PRequestRepository.findByRrnEntityId(rrnId);
        if(requestEntity != null) {
            log.error("findP2pDuplicateWithRrnId ({}) found", rrnId);
            throw new InternalServiceException("findP2pDuplicateWithRrnId", StatusRepositoryService.DUPLICATE_UUID, HttpStatus.OK);
        }
    }

    @Override
    public void findCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException {
        CashOutRequestEntity cashOutRequestEntity = cashOutRequestRepository.findByRrnEntityId(rrnId);
        if(cashOutRequestEntity != null) {
            log.error("cashOutDuplicateWithRrnId ({}) found", rrnId);
            throw new InternalServiceException("cashInDuplicateWithRrnId", StatusRepositoryService.DUPLICATE_UUID, HttpStatus.OK);
        }
    }

    @Override
    public void findPhysicalCashOutDuplicateWithRrnId(long rrnId) throws InternalServiceException {
        PhysicalCashOutRequestEntity physicalCashOutRequestEntity = physicalCashOutRequestRepository.findByRrnEntityId(rrnId);
        if(physicalCashOutRequestEntity != null) {
            log.error("cashOutDuplicateWithRrnId ({}) found", rrnId);
            throw new InternalServiceException("physicalCashOutDuplicateWithRrnId", StatusRepositoryService.DUPLICATE_UUID, HttpStatus.OK);
        }
    }

    @Override
    public AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDate(long[] walletAccountId, String transactionType, Date fromDate, Date toDate) {
        return reportPurchaseRequestRepository.findSumAmountByTransactionTypeBetweenDate(walletAccountId, transactionType, fromDate, toDate);
    }

    @Override
    public AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDateByChannelId(long channelId, String transactionType, Date fromDate, Date toDate) {
        return reportPurchaseRequestRepository.findSumAmountByTransactionTypeBetweenDateByChannel(channelId, transactionType, fromDate, toDate);
    }

    @Override
    public AggregationPurchaseDTO findPhysicalByChannelAndDate(long channelId, Date fromDate, Date toDate) {
        return null;
    }

    @Override
    public AggregationCashInDTO findSumAmountCashInBetweenDate(long[] walletAccountId, Date fromDate, Date toDate) {
        return cashInRequestRepository.findSumAmountBetweenDate(walletAccountId, fromDate, toDate);
    }

    @Override
    public AggregationCashOutDTO findSumAmountCashOutBetweenDate(long[] walletAccountId, Date fromDate, Date toDate) {
        return cashOutRequestRepository.findSumAmountBetweenDate(walletAccountId, fromDate, toDate);
    }

    @Override
    public AggregationCashOutDTO findSumAmountPhysicalCashOutBetweenDate(long[] walletAccountId, Date fromDate, Date toDate) {
        return physicalCashOutRequestRepository.findSumAmountBetweenDate(walletAccountId, fromDate, toDate);
    }

    @Override
    public AggregationP2PDTO findP2pSumAmountByTransactionTypeBetweenDate(long[] walletAccountId, Date fromDate, Date toDate) {
        return p2PRequestRepository.findSumAmountBetweenDate(walletAccountId, fromDate, toDate);
    }
}
