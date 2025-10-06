package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.ChannelOperationService;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.domain.request.setup.ChannelCreateRequestJson;
import com.melli.wallet.domain.request.setup.MerchantCreateRequestJson;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionObject;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
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
 * Class Name: GrpcPanelChannelService
 * Description: GRPC service implementation for panel channel operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPanelChannelService extends PanelChannelServiceGrpc.PanelChannelServiceImplBase {

    private final ChannelRepositoryService channelRepositoryService;
    private final WalletRepositoryService walletRepositoryService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final WalletOperationalService walletOperationalService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;
    private final ChannelOperationService channelOperationService;

    @Override
    public void createChannelWallet(ChannelCreateRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: CreateChannelWallet called with username: {}, nationalCode: {}",
                    request.getUsername(), request.getNationalCode());

            ChannelCreateRequestJson createRequest = new ChannelCreateRequestJson();
            createRequest.setUsername(request.getUsername());
            createRequest.setNationalCode(request.getNationalCode());

            ChannelEntity channel = channelRepositoryService.getChannel(createRequest.getUsername());
            if (channel == null) {
                log.error("channel with name ({}) is not exist", createRequest.getUsername());
                throw new InternalServiceException("channel not found", StatusRepositoryService.CHANNEL_NOT_FOUND, org.springframework.http.HttpStatus.OK);
            }

            if (channel.getWalletEntity() == null) {
                CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(
                        RequestContext.getChannelEntity(),
                        channel.getMobile(),
                        createRequest.getNationalCode(),
                        WalletTypeRepositoryService.CHANNEL,
                        List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                        List.of(WalletAccountTypeRepositoryService.WAGE)
                );
                channel.setWalletEntity(walletRepositoryService.findById(Long.parseLong(createWalletResponse.getWalletId())));
                channelRepositoryService.save(channel);
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setUuidResponse(UuidResponseGrpc.newBuilder()
                            .setUuid("wage wallet created for channel")
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: CreateChannelWallet completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: CreateChannelWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: CreateChannelWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void createMerchantWallet(MerchantCreateRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: CreateMerchantWallet called with name: {}, mobileNumber: {}",
                    request.getName(), request.getMobileNumber());

            MerchantCreateRequestJson createRequest = new MerchantCreateRequestJson();
            createRequest.setName(request.getName());
            createRequest.setMobileNumber(request.getMobileNumber());
            createRequest.setEconomicCode(request.getEconomicCode());

            String nationalCode = (createRequest.getEconomicCode().length() > 10) ?
                    createRequest.getEconomicCode().substring(0, 10) : createRequest.getEconomicCode();

            CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(
                    RequestContext.getChannelEntity(),
                    createRequest.getMobileNumber(),
                    nationalCode,
                    WalletTypeRepositoryService.MERCHANT,
                    List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                    List.of(WalletAccountTypeRepositoryService.NORMAL)
            );

            MerchantEntity merchantEntity = new MerchantEntity();
            merchantEntity.setName(createRequest.getName());
            merchantEntity.setDescription("create merchant");
            merchantEntity.setMobile(createRequest.getMobileNumber());
            merchantEntity.setEconomicalCode(createRequest.getEconomicCode());
            merchantEntity.setLogo("");
            merchantEntity.setWalletEntity(walletRepositoryService.findById(Long.parseLong(createWalletResponse.getWalletId())));
            merchantEntity.setSettlementType(1);
            merchantEntity.setStatus(1);
            merchantEntity.setCreatedBy(RequestContext.getChannelEntity().getUsername());
            merchantEntity.setCreatedAt(new Date());
            merchantRepositoryService.save(merchantEntity);

            MerchantWalletAccountCurrencyEntity goldEntity = new MerchantWalletAccountCurrencyEntity();
            goldEntity.setCreatedBy(RequestContext.getChannelEntity().getUsername());
            goldEntity.setCreatedAt(new Date());
            goldEntity.setMerchantEntity(merchantEntity);
            goldEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.GOLD));
            merchantWalletAccountCurrencyRepository.save(goldEntity);

            MerchantWalletAccountCurrencyEntity rialEntity = new MerchantWalletAccountCurrencyEntity();
            rialEntity.setCreatedBy(RequestContext.getChannelEntity().getUsername());
            rialEntity.setCreatedAt(new Date());
            rialEntity.setMerchantEntity(merchantEntity);
            rialEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL));
            merchantWalletAccountCurrencyRepository.save(rialEntity);

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setUuidResponse(UuidResponseGrpc.newBuilder()
                            .setUuid("merchant created successful")
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: CreateMerchantWallet completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: CreateMerchantWallet failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: CreateMerchantWallet unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getWageBalance(Empty request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetWageBalance called for channel: {}", RequestContext.getChannelEntity().getUsername());

            WalletBalanceResponse balanceResponse = channelOperationService.getBalance(
                    channelRepositoryService.findById(RequestContext.getChannelEntity().getId())
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

            log.info("GRPC: GetWageBalance completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetWageBalance failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetWageBalance unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void report(PanelBaseSearchRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Report called for channel: {}", RequestContext.getChannelEntity().getUsername());

            PanelBaseSearchJson searchRequest = new PanelBaseSearchJson();
            // Convert Map<String, String> from GRPC to Map<String, Object> for service
            Map<String, String> searchMap = new java.util.HashMap<>();
            for (var entry : request.getMapMap().entrySet()) {
                searchMap.put(entry.getKey(), entry.getValue());
            }
            searchRequest.setMap(searchMap);

            ReportTransactionResponse reportResponse = channelOperationService.report(
                    RequestContext.getChannelEntity(),
                    searchRequest.getMap()
            );

            // Convert to GRPC response
            ReportTransactionResponseGrpc.Builder reportResponseBuilder = ReportTransactionResponseGrpc.newBuilder();
            if (reportResponse != null && reportResponse.getList() != null) {
                for (var reportObject : reportResponse.getList()) {
                    ReportTransactionObjectGrpc reportObjectGrpc = ReportTransactionObjectGrpc.newBuilder()
                            .setId(reportObject.getId())
                            .setPurchaseType(reportObject.getType())
                            .setQuantity(reportObject.getQuantity())
                            .setBalance(reportObject.getBalance())
                            .setCreateTime(reportObject.getCreateTime() != null ? reportObject.getCreateTime() : "")
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