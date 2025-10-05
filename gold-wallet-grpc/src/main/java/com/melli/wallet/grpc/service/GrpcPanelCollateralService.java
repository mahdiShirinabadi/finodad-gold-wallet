package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.CollateralOperationService;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.domain.request.setup.PanelCollateralCreateRequestJson;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.response.collateral.CollateralListResponse;
import com.melli.wallet.domain.response.collateral.CollateralResponse;
import com.melli.wallet.domain.response.collateral.CollateralObject;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionObject;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.master.entity.CollateralEntity;
import com.melli.wallet.domain.master.entity.CollateralWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.CollateralWalletAccountCurrencyRepository;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class Name: GrpcPanelCollateralService
 * Description: GRPC service implementation for panel collateral operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPanelCollateralService extends PanelCollateralServiceGrpc.PanelCollateralServiceImplBase {

    private final WalletRepositoryService walletRepositoryService;
    private final CollateralRepositoryService collateralRepositoryService;
    private final CollateralOperationService collateralOperationService;
    private final WalletOperationalService walletOperationalService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final CollateralWalletAccountCurrencyRepository collateralWalletAccountCurrencyRepository;

    @Override
    public void createCollateralWallet(PanelCollateralCreateRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: CreateCollateralWallet called with name: {}, mobileNumber: {}", 
                request.getName(), request.getMobileNumber());
            
            PanelCollateralCreateRequestJson createRequest = new PanelCollateralCreateRequestJson();
            createRequest.setName(request.getName());
            createRequest.setMobileNumber(request.getMobileNumber());
            createRequest.setEconomicCode(request.getEconomicCode());
            createRequest.setIban(request.getIban());
            
            CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(
                RequestContext.getChannelEntity(), 
                createRequest.getMobileNumber(), 
                createRequest.getEconomicCode(), 
                WalletTypeRepositoryService.COLLATERAL,
                List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                List.of(WalletAccountTypeRepositoryService.NORMAL)
            );
            
            CollateralEntity collateralEntity = new CollateralEntity();
            collateralEntity.setName(createRequest.getName());
            collateralEntity.setDescription("create collateral");
            collateralEntity.setMobile(createRequest.getMobileNumber());
            collateralEntity.setEconomicalCode(createRequest.getEconomicCode());
            collateralEntity.setLogo("");
            collateralEntity.setWalletEntity(walletRepositoryService.findById(Long.parseLong(createWalletResponse.getWalletId())));
            collateralEntity.setIban(createRequest.getIban());
            collateralEntity.setStatus(1);
            collateralEntity.setCreatedBy(RequestContext.getChannelEntity().getUsername());
            collateralEntity.setCreatedAt(new Date());
            collateralRepositoryService.save(collateralEntity);

            CollateralWalletAccountCurrencyEntity goldEntity = new CollateralWalletAccountCurrencyEntity();
            goldEntity.setCreatedBy(RequestContext.getChannelEntity().getUsername());
            goldEntity.setCreatedAt(new Date());
            goldEntity.setCollateralEntity(collateralEntity);
            goldEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.GOLD));
            collateralWalletAccountCurrencyRepository.save(goldEntity);

            CollateralWalletAccountCurrencyEntity rialEntity = new CollateralWalletAccountCurrencyEntity();
            rialEntity.setCreatedBy(RequestContext.getChannelEntity().getUsername());
            rialEntity.setCreatedAt(new Date());
            rialEntity.setCollateralEntity(collateralEntity);
            rialEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL));
            collateralWalletAccountCurrencyRepository.save(rialEntity);
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid(String.valueOf(collateralEntity.getId()))
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: CreateCollateralWallet completed successfully with ID: {}", collateralEntity.getId());
            
        } catch (InternalServiceException e) {
            log.error("GRPC: CreateCollateralWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: CreateCollateralWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void list(GetCollateralListRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: List called with currency: {}", request.getCurrency());
            
            CollateralResponse collateralResponse = collateralRepositoryService.get(
                RequestContext.getChannelEntity(), 
                request.getCurrency()
            );
            
            // Convert to GRPC response
            CollateralResponseGrpc.Builder collateralResponseBuilder = CollateralResponseGrpc.newBuilder();
            if (collateralResponse != null && collateralResponse.getCollateralObjectList() != null) {
                for (var collateralObject : collateralResponse.getCollateralObjectList()) {
                    CollateralObjectGrpc collateralObjectGrpc = CollateralObjectGrpc.newBuilder()
                        .setId(collateralObject.getId() != null ? collateralObject.getId() : "")
                        .setName(collateralObject.getName() != null ? collateralObject.getName() : "")
                        .setLogo(collateralObject.getLogo() != null ? collateralObject.getLogo() : "")
                        .build();
                    collateralResponseBuilder.addCollateralObjectList(collateralObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCollateralResponse(collateralResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: List completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: List failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: List unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void createList(PanelBaseSearchRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: CreateList called for collateral");
            
            PanelBaseSearchJson searchRequest = new PanelBaseSearchJson();
            // Convert Map<String, String> from GRPC to Map<String, Object> for service
            Map<String, Object> searchMap = new java.util.HashMap<>();
            for (var entry : request.getMapMap().entrySet()) {
                searchMap.put(entry.getKey(), entry.getValue());
            }
            searchRequest.setMap(searchMap);
            
            CollateralListResponse listResponse = collateralOperationService.list(
                RequestContext.getChannelEntity(), 
                searchRequest.getMap()
            );
            
            // Convert to GRPC response
            CollateralListResponseGrpc.Builder listResponseBuilder = CollateralListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getCollateralObjectList() != null) {
                for (var collateralObject : listResponse.getCollateralObjectList()) {
                    CollateralObjectGrpc collateralObjectGrpc = CollateralObjectGrpc.newBuilder()
                        .setId(collateralObject.getId() != null ? collateralObject.getId() : "")
                        .setName(collateralObject.getName() != null ? collateralObject.getName() : "")
                        .setLogo(collateralObject.getLogo() != null ? collateralObject.getLogo() : "")
                        .build();
                    listResponseBuilder.addCollateralObjectList(collateralObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCollateralListResponse(listResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: CreateList completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: CreateList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: CreateList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getBalance(GetCollateralBalanceRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetBalance called with collateralId: {}", request.getCollateralId());
            
            WalletBalanceResponse balanceResponse = collateralOperationService.getBalance(
                RequestContext.getChannelEntity(), 
                request.getCollateralId()
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
    public void report(PanelBaseSearchRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Report called for collateral");
            
            PanelBaseSearchJson searchRequest = new PanelBaseSearchJson();
            // Convert Map<String, String> from GRPC to Map<String, Object> for service
            Map<String, Object> searchMap = new java.util.HashMap<>();
            for (var entry : request.getMapMap().entrySet()) {
                searchMap.put(entry.getKey(), entry.getValue());
            }
            searchRequest.setMap(searchMap);
            
            ReportTransactionResponse reportResponse = collateralOperationService.report(
                RequestContext.getChannelEntity(), 
                searchRequest.getMap()
            );
            
            // Convert to GRPC response
            ReportTransactionResponseGrpc.Builder reportResponseBuilder = ReportTransactionResponseGrpc.newBuilder();
            if (reportResponse != null && reportResponse.getReportTransactionObjectList() != null) {
                for (var reportObject : reportResponse.getReportTransactionObjectList()) {
                    ReportTransactionObjectGrpc reportObjectGrpc = ReportTransactionObjectGrpc.newBuilder()
                        .setId(reportObject.getId())
                        .setTransactionType(reportObject.getTransactionType() != null ? reportObject.getTransactionType() : "")
                        .setAmount(reportObject.getAmount() != null ? reportObject.getAmount() : "")
                        .setBalance(reportObject.getBalance() != null ? reportObject.getBalance() : "")
                        .setDescription(reportObject.getDescription() != null ? reportObject.getDescription() : "")
                        .setCreateTime(reportObject.getCreateTime() != null ? reportObject.getCreateTime() : "")
                        .setCreateTimeTimestamp(reportObject.getCreateTimeTimestamp())
                        .build();
                    reportResponseBuilder.addReportTransactionObjectList(reportObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setReportTransactionResponse(reportResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Report completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Report failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Report unexpected error: {}", e.getMessage(), e);
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
