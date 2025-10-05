package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import com.melli.wallet.service.operation.MerchantOperationService;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

/**
 * Class Name: GrpcMerchantService
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 * 
 * This service implements GRPC methods for Merchant operations.
 * It mirrors the functionality of MerchantController REST endpoints.
 */
@GrpcService
@Service
@RequiredArgsConstructor
@Log4j2
public class GrpcMerchantService extends MerchantServiceGrpc.MerchantServiceImplBase {

    private final MerchantRepositoryService merchantRepositoryService;
    private final MerchantOperationService merchantOperationService;

    @Override
    public void merchantList(MerchantListRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetMerchantList called with currency: {}", request.getCurrency());
            
            MerchantResponse merchantResponse = merchantRepositoryService.getMerchant(
                RequestContext.getChannelEntity(),
                request.getCurrency()
            );
            
            // Convert to GRPC response
            MerchantResponseGrpc.Builder merchantResponseBuilder = MerchantResponseGrpc.newBuilder();
            if (merchantResponse != null && merchantResponse.getMerchantObjectList() != null) {
                for (var merchantObject : merchantResponse.getMerchantObjectList()) {
                    MerchantObjectGrpc merchantObjectGrpc = MerchantObjectGrpc.newBuilder()
                        .setId(merchantObject.getId() != null ? merchantObject.getId() : "")
                        .setName(merchantObject.getName() != null ? merchantObject.getName() : "")
                        .setLogo(merchantObject.getLogo() != null ? merchantObject.getLogo() : "")
                        .build();
                    merchantResponseBuilder.addMerchantObjectList(merchantObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setMerchantResponse(merchantResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GetMerchantList completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GetMerchantList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetMerchantList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void balanceMerchant(MerchantBalanceRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetBalance called with merchantId: {}", request.getMerchantId());
            
            WalletBalanceResponse balanceResponse = merchantOperationService.getBalance(
                RequestContext.getChannelEntity(),
                request.getMerchantId()
            );
            
            // Convert to GRPC response
            WalletBalanceResponseGrpc.Builder balanceResponseBuilder = WalletBalanceResponseGrpc.newBuilder();
            if (balanceResponse != null && balanceResponse.getWalletAccountObjectList() != null) {
                for (var walletAccountObject : balanceResponse.getWalletAccountObjectList()) {
                    // Build WalletAccountTypeObjectGrpc
                    WalletAccountTypeObjectGrpc walletAccountTypeObjectGrpc = WalletAccountTypeObjectGrpc.newBuilder()
                        .setId(walletAccountObject.getWalletAccountTypeObject().getId() != null ? walletAccountObject.getWalletAccountTypeObject().getId() : "")
                        .setName(walletAccountObject.getWalletAccountTypeObject().getName() != null ? walletAccountObject.getWalletAccountTypeObject().getName() : "")
                        .build();
                    
                    // Build WalletAccountCurrencyObjectGrpc
                    WalletAccountCurrencyObjectGrpc walletAccountCurrencyObjectGrpc = WalletAccountCurrencyObjectGrpc.newBuilder()
                        .setId(walletAccountObject.getWalletAccountCurrencyObject().getId() != null ? walletAccountObject.getWalletAccountCurrencyObject().getId() : "")
                        .setName(walletAccountObject.getWalletAccountCurrencyObject().getName() != null ? walletAccountObject.getWalletAccountCurrencyObject().getName() : "")
                        .build();
                    
                    // Build WalletAccountObjectGrpc
                    WalletAccountObjectGrpc walletAccountObjectGrpc = WalletAccountObjectGrpc.newBuilder()
                        .setWalletAccountTypeObject(walletAccountTypeObjectGrpc)
                        .setWalletAccountCurrencyObject(walletAccountCurrencyObjectGrpc)
                        .setAccountNumber(walletAccountObject.getAccountNumber() != null ? walletAccountObject.getAccountNumber() : "")
                        .setBalance(walletAccountObject.getBalance() != null ? walletAccountObject.getBalance() : "")
                        .setAvailableBalance(walletAccountObject.getAvailableBalance() != null ? walletAccountObject.getAvailableBalance() : "")
                        .setStatus(walletAccountObject.getStatus() != null ? walletAccountObject.getStatus() : "")
                        .setStatusDescription(walletAccountObject.getStatusDescription() != null ? walletAccountObject.getStatusDescription() : "")
                        .build();
                    
                    balanceResponseBuilder.addWalletAccountObjectList(walletAccountObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setWalletBalanceResponse(balanceResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GetBalance completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GetBalance failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetBalance unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void updateStatus(MerchantUpdateRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: UpdateStatus called with merchantId: {}, status: {}", 
                request.getMerchantId(), request.getStatus());
            
            merchantOperationService.updateStatus(
                RequestContext.getChannelEntity(),
                request.getMerchantId(), 
                String.valueOf(request.getStatus())
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: UpdateStatus completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: UpdateStatus failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: UpdateStatus unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void increaseBalance(IncreaseMerchantBalanceRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: IncreaseBalance called with merchantId: {}, amount: {}, walletAccountNumber: {}", 
                request.getMerchantId(), request.getAmount(), request.getWalletAccountNumber());
            
            String traceId = merchantOperationService.increaseBalance(
                RequestContext.getChannelEntity(),
                request.getWalletAccountNumber(),
                request.getAmount(),
                request.getMerchantId()
            );
            
            String successMessage = "Balance increased successfully. TraceId: " + traceId;
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid(successMessage)
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: IncreaseBalance completed successfully with traceId: {}", traceId);
            
        } catch (InternalServiceException e) {
            log.error("GRPC: IncreaseBalance failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: IncreaseBalance unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void decreaseBalance(DecreaseMerchantBalanceRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: DecreaseBalance called with merchantId: {}, amount: {}, walletAccountNumber: {}", 
                request.getMerchantId(), request.getAmount(), request.getWalletAccountNumber());
            
            String traceId = merchantOperationService.decreaseBalance(
                RequestContext.getChannelEntity(),
                request.getWalletAccountNumber(),
                request.getAmount(),
                request.getMerchantId()
            );
            
            String successMessage = "Balance decreased successfully. TraceId: " + traceId;
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid(successMessage)
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: DecreaseBalance completed successfully with traceId: {}", traceId);
            
        } catch (InternalServiceException e) {
            log.error("GRPC: DecreaseBalance failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: DecreaseBalance unexpected error: {}", e.getMessage(), e);
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