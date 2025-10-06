package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.WalletListOperationService;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.response.panel.CustomerListResponse;
import com.melli.wallet.domain.response.panel.WalletAccountCurrencyListResponse;
import com.melli.wallet.domain.response.panel.WalletAccountTypeListResponse;
import com.melli.wallet.domain.response.panel.WalletLevelListResponse;
import com.melli.wallet.domain.response.panel.WalletTypeListResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class Name: GrpcPanelWalletService
 * Description: GRPC service implementation for panel wallet operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPanelWalletService extends PanelWalletServiceGrpc.PanelWalletServiceImplBase {

    private final WalletListOperationService walletListOperationService;

    @Override
    public void getWalletAccountCurrencyList(Empty request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetWalletAccountCurrencyList called");

            WalletAccountCurrencyListResponse listResponse = walletListOperationService.getWalletAccountCurrencyList();

            // Convert to GRPC response
            WalletAccountCurrencyListResponseGrpc.Builder listResponseBuilder = WalletAccountCurrencyListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getWalletAccountCurrencies() != null) {
                for (var currencyObject : listResponse.getWalletAccountCurrencies()) {
                    WalletAccountCurrencyObjectGrpc currencyObjectGrpc = WalletAccountCurrencyObjectGrpc.newBuilder()
                            .setId(currencyObject.getId())
                            .setName(currencyObject.getName() != null ? currencyObject.getName() : "")
                            .setSuffix(currencyObject.getSuffix() != null ? currencyObject.getSuffix() : "")
                            .setAdditionalData(currencyObject.getAdditionalData() != null ? currencyObject.getAdditionalData() : "")
                            .setDescription(currencyObject.getDescription() != null ? currencyObject.getDescription() : "")
                            .build();
                    listResponseBuilder.addWalletAccountCurrencyObjectList(currencyObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setWalletAccountCurrencyListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetWalletAccountCurrencyList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetWalletAccountCurrencyList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetWalletAccountCurrencyList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getWalletLevelList(Empty request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetWalletLevelList called");

            WalletLevelListResponse listResponse = walletListOperationService.getWalletLevelList();

            // Convert to GRPC response
            WalletLevelListResponseGrpc.Builder listResponseBuilder = WalletLevelListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getWalletLevels() != null) {
                for (var levelObject : listResponse.getWalletLevels()) {
                    WalletLevelObjectGrpc levelObjectGrpc = WalletLevelObjectGrpc.newBuilder()
                            .setId(levelObject.getId())
                            .setName(levelObject.getName() != null ? levelObject.getName() : "")
                            .setDescription(levelObject.getAdditionalData() != null ? levelObject.getAdditionalData() : "")
                            .build();
                    listResponseBuilder.addWalletLevelObjectList(levelObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setWalletLevelListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetWalletLevelList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetWalletLevelList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetWalletLevelList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getWalletTypeList(Empty request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetWalletTypeList called");

            WalletTypeListResponse listResponse = walletListOperationService.getWalletTypeList();

            // Convert to GRPC response
            WalletTypeListResponseGrpc.Builder listResponseBuilder = WalletTypeListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getWalletTypes() != null) {
                for (var typeObject : listResponse.getWalletTypes()) {
                    WalletTypeObjectGrpc typeObjectGrpc = WalletTypeObjectGrpc.newBuilder()
                            .setId(typeObject.getId())
                            .setName(typeObject.getName() != null ? typeObject.getName() : "")
                            .setDescription(typeObject.getDescription() != null ? typeObject.getDescription() : "")
                            .build();
                    listResponseBuilder.addWalletTypeObjectList(typeObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setWalletTypeListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetWalletTypeList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetWalletTypeList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetWalletTypeList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getWalletAccountTypeList(Empty request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetWalletAccountTypeList called");

            WalletAccountTypeListResponse listResponse = walletListOperationService.getWalletAccountTypeList();

            // Convert to GRPC response
            WalletAccountTypeListResponseGrpc.Builder listResponseBuilder = WalletAccountTypeListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getWalletAccountTypes() != null) {
                for (var accountTypeObject : listResponse.getWalletAccountTypes()) {
                    WalletAccountTypeObjectGrpc accountTypeObjectGrpc = WalletAccountTypeObjectGrpc.newBuilder()
                            .setId(accountTypeObject.getId())
                            .setName(accountTypeObject.getName() != null ? accountTypeObject.getName() : "")
                            .setDescription(accountTypeObject.getDescription() != null ? accountTypeObject.getDescription() : "")
                            .build();
                    listResponseBuilder.addWalletAccountTypeObjectList(accountTypeObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setWalletAccountTypeListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetWalletAccountTypeList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetWalletAccountTypeList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetWalletAccountTypeList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getCustomerList(PanelBaseSearchRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetCustomerList called");

            PanelBaseSearchJson searchRequest = new PanelBaseSearchJson();
            // Convert Map<String, String> from GRPC to Map<String, Object> for service
            Map<String, String> searchMap = new java.util.HashMap<>(request.getMapMap());
            searchRequest.setMap(searchMap);

            CustomerListResponse listResponse = walletListOperationService.getCustomerListEfficient(searchRequest.getMap());

            // Convert to GRPC response
            CustomerListResponseGrpc.Builder listResponseBuilder = CustomerListResponseGrpc.newBuilder()
                    .setTotalElements(listResponse.getTotalElements())
                    .setTotalPages(listResponse.getTotalPages())
                    .setCurrentPage(listResponse.getCurrentPage())
                    .setPageSize(listResponse.getPageSize());

            if (listResponse != null && listResponse.getCustomers() != null) {
                for (var customerObject : listResponse.getCustomers()) {
                    CustomerObjectGrpc customerObjectGrpc = CustomerObjectGrpc.newBuilder()
                            .setId(customerObject.getWallet().getWalletId())
                            .setNationalCode(customerObject.getWallet().getNationalCode() != null ? customerObject.getWallet().getNationalCode() : "")
                            .setMobile(customerObject.getWallet().getMobile() != null ? customerObject.getWallet().getMobile() : "")
                            .setStatus(customerObject.getWallet().getStatus() != null ? customerObject.getWallet().getStatus() : "")
//                            .setStatusDescription(customerObject.getWallet().getStatusDescription() != null ? customerObject.getStatusDescription() : "")
                            .setCreateTime(customerObject.getWallet().getCreateTime() != null ? customerObject.getWallet().getCreateTime() : "")
//                            .setCreateTimeTimestamp(customerObject.getWallet().getCreateTimeTimestamp())
                            .build();
                    listResponseBuilder.addCustomerObjectList(customerObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setCustomerListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetCustomerList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetCustomerList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetCustomerList unexpected error: {}", e.getMessage(), e);
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
