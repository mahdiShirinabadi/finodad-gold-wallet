package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.repository.LimitationGeneralService;
import com.melli.wallet.service.repository.LimitationGeneralCustomRepositoryService;
import com.melli.wallet.service.operation.LimitationOperationService;
import com.melli.wallet.domain.request.limitation.CreateLimitationGeneralCustomRequestJson;
import com.melli.wallet.domain.request.limitation.UpdateLimitationGeneralRequestJson;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.response.limitation.GeneralCustomLimitationListResponse;
import com.melli.wallet.domain.response.limitation.GeneralLimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class Name: GrpcPanelLimitationService
 * Description: GRPC service implementation for panel limitation operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPanelLimitationService extends PanelLimitationServiceGrpc.PanelLimitationServiceImplBase {

    private final LimitationGeneralService limitationGeneralService;
    private final LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    private final LimitationOperationService limitationOperationService;

    @Override
    public void updateLimitationGeneral(UpdateLimitationGeneralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: UpdateLimitationGeneral called with id: {}, value: {}", 
                request.getId(), request.getValue());
            
            UpdateLimitationGeneralRequestJson updateRequest = new UpdateLimitationGeneralRequestJson();
            updateRequest.setId(request.getId());
            updateRequest.setValue(request.getValue());
            updateRequest.setPattern(request.getPattern());
            
            limitationOperationService.updateLimitationGeneral(
                Long.parseLong(updateRequest.getId()), 
                updateRequest.getValue(), 
                updateRequest.getPattern(), 
                RequestContext.getChannelEntity()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid("General limitation updated successfully")
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: UpdateLimitationGeneral completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: UpdateLimitationGeneral failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: UpdateLimitationGeneral unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void insertLimitationGeneralCustom(CreateLimitationGeneralCustomRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: InsertLimitationGeneralCustom called with limitationGeneralId: {}, value: {}", 
                request.getLimitationGeneralId(), request.getValue());
            
            CreateLimitationGeneralCustomRequestJson createRequest = new CreateLimitationGeneralCustomRequestJson();
            createRequest.setLimitationGeneralId(request.getLimitationGeneralId());
            createRequest.setValue(request.getValue());
            createRequest.setAdditionalData(request.getAdditionalData());
            createRequest.setWalletLevelId(request.getWalletLevelId());
            createRequest.setWalletAccountTypeId(request.getWalletAccountTypeId());
            createRequest.setWalletAccountCurrencyId(request.getWalletAccountCurrencyId());
            createRequest.setWalletTypeId(request.getWalletTypeId());
            createRequest.setChannelId(request.getChannelId());
            
            limitationOperationService.insertLimitationGeneralCustom(
                Long.parseLong(createRequest.getLimitationGeneralId()),
                createRequest.getValue(),
                createRequest.getAdditionalData(),
                createRequest.getWalletLevelId() != null ? Long.parseLong(createRequest.getWalletLevelId()) : null,
                createRequest.getWalletAccountTypeId() != null ? Long.parseLong(createRequest.getWalletAccountTypeId()) : null,
                createRequest.getWalletAccountCurrencyId() != null ? Long.parseLong(createRequest.getWalletAccountCurrencyId()) : null,
                createRequest.getWalletTypeId() != null ? Long.parseLong(createRequest.getWalletTypeId()) : null,
                createRequest.getChannelId() != null ? Long.parseLong(createRequest.getChannelId()) : null,
                RequestContext.getChannelEntity()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid("General custom limitation inserted successfully")
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: InsertLimitationGeneralCustom completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: InsertLimitationGeneralCustom failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: InsertLimitationGeneralCustom unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }


    @Override
    public void getGeneralLimitationsList(GetGeneralLimitationsListRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetGeneralLimitationsList called");

            PanelBaseSearchJson searchRequest = new PanelBaseSearchJson();
            // Convert Map<String, String> from GRPC to Map<String, Object> for service
            Map<String, String> searchMap = new java.util.HashMap<>(request.getSearchMapMap());
            searchRequest.setMap(searchMap);

            GeneralLimitationListResponse listResponse = limitationGeneralService.getGeneralLimitationList(
                    RequestContext.getChannelEntity(),
                    searchRequest.getMap()
            );

            // Convert to GRPC response
            GeneralLimitationListResponseGrpc.Builder listResponseBuilder = GeneralLimitationListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getGeneralLimitationList() != null) {
                for (var limitationObject : listResponse.getGeneralLimitationList()) {
                    GeneralLimitationObjectGrpc limitationObjectGrpc = GeneralLimitationObjectGrpc.newBuilder()
                            .setId(limitationObject.getId() != null ? limitationObject.getId() : "")
                            .setName(limitationObject.getName() != null ? limitationObject.getName() : "")
                            .setValue(limitationObject.getValue() != null ? limitationObject.getValue() : "")
                            .setPattern(limitationObject.getPattern() != null ? limitationObject.getPattern() : "")
//                        .setDescription(limitationObject.get() != null ? limitationObject.getDescription() : "")
                            .setCreateTime(limitationObject.getCreateTime() != null ? limitationObject.getCreateTime() : "")
//                        .setCreateTimeTimestamp(limitationObject.getCreateTimeTimestamp())
                            .build();
                    listResponseBuilder.addGeneralLimitationObjectList(limitationObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setGeneralLimitationListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetGeneralLimitationsList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetGeneralLimitationsList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetGeneralLimitationsList unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getGeneralCustomLimitationsList(GetGeneralCustomLimitationsListRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetGeneralCustomLimitationsList called");

            PanelBaseSearchJson searchRequest = new PanelBaseSearchJson();
            // Convert Map<String, String> from GRPC to Map<String, Object> for service
            Map<String, String> searchMap = new java.util.HashMap<>(request.getSearchMapMap());
            searchRequest.setMap(searchMap);

            GeneralCustomLimitationListResponse listResponse = limitationGeneralCustomRepositoryService.getGeneralCustomLimitationList(
                    RequestContext.getChannelEntity(),
                    searchRequest.getMap()
            );

            // Convert to GRPC response
            GeneralCustomLimitationListResponseGrpc.Builder listResponseBuilder = GeneralCustomLimitationListResponseGrpc.newBuilder();
            if (listResponse != null && listResponse.getGeneralCustomLimitationList() != null) {
                for (var customLimitationObject : listResponse.getGeneralCustomLimitationList()) {
                    GeneralCustomLimitationObjectGrpc customLimitationObjectGrpc = GeneralCustomLimitationObjectGrpc.newBuilder()
                            .setId(customLimitationObject.getId() != null ? customLimitationObject.getId() : "")
                            .setLimitationGeneralId(customLimitationObject.getLimitationGeneralId() != null ? customLimitationObject.getLimitationGeneralId() : "")
                            .setValue(customLimitationObject.getValue() != null ? customLimitationObject.getValue() : "")
                            .setAdditionalData(customLimitationObject.getAdditionalData() != null ? customLimitationObject.getAdditionalData() : "")
                            .setWalletLevelId(customLimitationObject.getWalletLevelId() != null ? customLimitationObject.getWalletLevelId() : "")
                            .setWalletAccountTypeId(customLimitationObject.getWalletAccountTypeId() != null ? customLimitationObject.getWalletAccountTypeId() : "")
                            .setWalletAccountCurrencyId(customLimitationObject.getWalletAccountCurrencyId() != null ? customLimitationObject.getWalletAccountCurrencyId() : "")
                            .setWalletTypeId(customLimitationObject.getWalletTypeId() != null ? customLimitationObject.getWalletTypeId() : "")
                            .setChannelId(customLimitationObject.getChannelId() != null ? customLimitationObject.getChannelId() : "")
                            .setCreateTime(customLimitationObject.getCreateTime() != null ? customLimitationObject.getCreateTime() : "")
//                        .setCreateTimeTimestamp(customLimitationObject.get())
                            .build();
                    listResponseBuilder.addGeneralCustomLimitationObjectList(customLimitationObjectGrpc);
                }
            }

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setGeneralCustomLimitationListResponse(listResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GetGeneralCustomLimitationsList completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: GetGeneralCustomLimitationsList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetGeneralCustomLimitationsList unexpected error: {}", e.getMessage(), e);
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
