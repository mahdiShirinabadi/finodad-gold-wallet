package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.repository.TransactionRepositoryService;
import com.melli.wallet.domain.response.transaction.StatementResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class Name: GrpcTransactionService
 * Description: GRPC service implementation for transaction operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcTransactionService extends TransactionServiceGrpc.TransactionServiceImplBase {

    private final TransactionRepositoryService transactionRepositoryService;

    @Override
    public void getLastTransaction(GetLastTransactionRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetLastTransaction called with nationalCode: {}, accountNumber: {}", 
                request.getNationalCode(), request.getAccountNumber());
            
            StatementResponse statementResponse = transactionRepositoryService.lastTransaction(
                RequestContext.getChannelEntity(),
                request.getNationalCode(),
                request.getAccountNumber(),
                100
            );
            
            // Convert to GRPC response
            StatementResponseGrpc.Builder statementResponseBuilder = StatementResponseGrpc.newBuilder()
                .setNationalCode(statementResponse.getNationalCode() != null ? statementResponse.getNationalCode() : "");
            
            if (statementResponse.getList() != null) {
                for (var transactionObject : statementResponse.getList()) {
                    StatementObjectGrpc statementObjectGrpc = StatementObjectGrpc.newBuilder()
                        .setId(Long.parseLong(transactionObject.getId()))
                        .setTransactionType(transactionObject.getType() != null ? transactionObject.getType() : "")
                        .setAmount(transactionObject.getQuantity() != null ? transactionObject.getQuantity() : "")
                        .setBalance(transactionObject.getBalance() != null ? transactionObject.getBalance() : "")
                        .setCreateTime(transactionObject.getCreateTime() != null ? transactionObject.getCreateTime() : "")
                        .build();
                    statementResponseBuilder.addTransactionObjectList(statementObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setStatementResponse(statementResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GetLastTransaction completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GetLastTransaction failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetLastTransaction unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void reportTransaction(ReportTransactionRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: ReportTransaction called with searchMap size: {}", request.getSearchMapMap().size());
            
            Map<String, String> searchMap = request.getSearchMapMap();
            ReportTransactionResponse reportResponse = transactionRepositoryService.reportTransaction(
                RequestContext.getChannelEntity(),
                searchMap
            );
            
            // Convert to GRPC response
            ReportTransactionResponseGrpc.Builder reportResponseBuilder = ReportTransactionResponseGrpc.newBuilder();
            if (reportResponse != null && reportResponse.getList() != null) {
                for (var transactionObject : reportResponse.getList()) {
                    TransactionObjectGrpc transactionObjectGrpc = TransactionObjectGrpc.newBuilder()
                        .setId(Long.parseLong(transactionObject.getId()))
                        .setNationalCode(transactionObject.getNationalCode() != null ? transactionObject.getNationalCode() : "")
                        .setTransactionType(transactionObject.getType() != null ? transactionObject.getType() : "")
                        .setAmount(transactionObject.getQuantity() != null ? transactionObject.getQuantity() : "")
                        .setBalance(transactionObject.getBalance() != null ? transactionObject.getBalance() : "")
                        .setCreateTime(transactionObject.getCreateTime() != null ? transactionObject.getCreateTime() : "")
                        .build();
                    reportResponseBuilder.addTransactionList(transactionObjectGrpc);
                }
            }
            
            reportResponseBuilder
                .setTotalPages(reportResponse != null ? (int) reportResponse.getTotalPages() : 0)
                .setTotalElements(reportResponse != null ? reportResponse.getTotalElements() : 0)
                .setCurrentPage(reportResponse != null ? reportResponse.getNumber() : 0)
                .setPageSize(reportResponse != null ? reportResponse.getSize() : 0);
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setReportTransactionResponse(reportResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: ReportTransaction completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: ReportTransaction failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: ReportTransaction unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    private void handleError(StreamObserver<BaseResponseGrpc> responseObserver, InternalServiceException e) {
        BaseResponseGrpc errorResponse = BaseResponseGrpc.newBuilder()
                .setSuccess(false)
                .setErrorDetail(ErrorDetailGrpc.newBuilder()
                        .setCode(String.valueOf(e.getStatus()))
                        .setMessage(e.getMessage() != null ? e.getMessage() : "Unknown error")
                        .build())
                .build();

        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
    }

    private void handleUnexpectedError(StreamObserver<BaseResponseGrpc> responseObserver, Exception e) {
        BaseResponseGrpc errorResponse = BaseResponseGrpc.newBuilder()
            .setSuccess(false)
            .setErrorDetail(ErrorDetailGrpc.newBuilder()
                .setCode("UNEXPECTED_ERROR")
                .setMessage("An unexpected error occurred: " + e.getMessage())
                .build())
            .build();
        
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
    }
}
