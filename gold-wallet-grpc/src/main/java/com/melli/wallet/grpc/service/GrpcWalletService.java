package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.WalletTypeRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.service.repository.WalletAccountTypeRepositoryService;
import com.melli.wallet.util.Utility;
import com.melli.wallet.domain.request.wallet.CreateWalletRequestJson;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: GrpcWalletService
 * Description: GRPC service implementation for wallet operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcWalletService extends WalletServiceGrpc.WalletServiceImplBase {

    private final WalletOperationalService walletOperationalService;

    @Override
    public void createWallet(CreateWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: CreateWallet called with mobile: {}, nationalCode: {}", 
                request.getMobile(), request.getNationalCode());
            
            CreateWalletRequestJson createWalletRequest = new CreateWalletRequestJson();
            createWalletRequest.setMobile(request.getMobile());
            createWalletRequest.setNationalCode(request.getNationalCode());
            
            String cleanMobile = Utility.cleanPhoneNumber(createWalletRequest.getMobile());
            CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(
                RequestContext.getChannelEntity(),
                cleanMobile,
                createWalletRequest.getNationalCode(),
                WalletTypeRepositoryService.NORMAL_USER,
                List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                List.of(WalletAccountTypeRepositoryService.NORMAL)
            );
            
            // Convert to GRPC response
            CreateWalletResponseGrpc.Builder createWalletResponseBuilder = CreateWalletResponseGrpc.newBuilder()
                .setWalletId(createWalletResponse.getWalletId() != null ? createWalletResponse.getWalletId() : "")
                .setMobile(createWalletResponse.getMobile() != null ? createWalletResponse.getMobile() : "")
                .setNationalCode(createWalletResponse.getNationalCode() != null ? createWalletResponse.getNationalCode() : "")
                .setStatus(createWalletResponse.getStatus() != null ? createWalletResponse.getStatus() : "")
                .setStatusDescription(createWalletResponse.getStatusDescription() != null ? createWalletResponse.getStatusDescription() : "");
            
            if (createWalletResponse.getWalletAccountObjectList() != null) {
                for (var walletAccountObject : createWalletResponse.getWalletAccountObjectList()) {
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
                    
                    createWalletResponseBuilder.addWalletAccountObjectList(walletAccountObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCreateWalletResponse(createWalletResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: CreateWallet completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: CreateWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: CreateWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void deactivateWallet(DeactivateWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: DeactivateWallet called with id: {}", request.getId());
            
            walletOperationalService.deactivateWallet(
                RequestContext.getChannelEntity(),
                request.getId(),
                RequestContext.getClientIp()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: DeactivateWallet completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: DeactivateWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: DeactivateWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void deleteWallet(DeleteWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: DeleteWallet called with id: {}", request.getId());
            
            walletOperationalService.deleteWallet(
                RequestContext.getChannelEntity(),
                request.getId(),
                RequestContext.getClientIp()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: DeleteWallet completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: DeleteWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: DeleteWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getWallet(GetWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetWallet called with nationalCode: {}", request.getNationalCode());
            
            CreateWalletResponse getWalletResponse = walletOperationalService.get(
                RequestContext.getChannelEntity(),
                request.getNationalCode()
            );
            
            // Convert to GRPC response
            CreateWalletResponseGrpc.Builder createWalletResponseBuilder = CreateWalletResponseGrpc.newBuilder()
                .setWalletId(getWalletResponse.getWalletId() != null ? getWalletResponse.getWalletId() : "")
                .setMobile(getWalletResponse.getMobile() != null ? getWalletResponse.getMobile() : "")
                .setNationalCode(getWalletResponse.getNationalCode() != null ? getWalletResponse.getNationalCode() : "")
                .setStatus(getWalletResponse.getStatus() != null ? getWalletResponse.getStatus() : "")
                .setStatusDescription(getWalletResponse.getStatusDescription() != null ? getWalletResponse.getStatusDescription() : "");
            
            if (getWalletResponse.getWalletAccountObjectList() != null) {
                for (var walletAccountObject : getWalletResponse.getWalletAccountObjectList()) {
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
                    
                    createWalletResponseBuilder.addWalletAccountObjectList(walletAccountObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCreateWalletResponse(createWalletResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GetWallet completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GetWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void activateWallet(ActivateWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: ActivateWallet called with id: {}", request.getId());
            
            walletOperationalService.activateWallet(
                RequestContext.getChannelEntity(),
                request.getId(),
                RequestContext.getClientIp()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: ActivateWallet completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: ActivateWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: ActivateWallet unexpected error: {}", e.getMessage(), e);
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