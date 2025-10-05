package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.LimitationOperationService;
import com.melli.wallet.domain.response.limitation.LimitationCustomResponse;
import com.melli.wallet.domain.response.limitation.LimitationListResponse;
import com.melli.wallet.domain.response.limitation.LimitationObject;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

/**
 * Class Name: GrpcLimitationService
 * Description: GRPC service implementation for limitation operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcLimitationService extends LimitationServiceGrpc.LimitationServiceImplBase {

    private final LimitationOperationService limitationOperationService;

    @Override
    public void getValue(GetValueLimitationRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetValue called with limitationName: {}, accountNumber: {}, nationalCode: {}", 
                request.getLimitationName(), request.getAccountNumber(), request.getNationalCode());
            
            String value = limitationOperationService.getValue(
                RequestContext.getChannelEntity(),
                request.getLimitationName(),
                request.getAccountNumber(),
                request.getNationalCode(),
                RequestContext.getClientIp()
            );
            
            // Convert to GRPC response
            LimitationCustomResponseGrpc limitationCustomResponseGrpc = LimitationCustomResponseGrpc.newBuilder()
                .setValue(value != null ? value : "")
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setLimitationCustomResponse(limitationCustomResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GetValue completed successfully with value: {}", value);
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GetValue failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetValue unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void getList(Empty request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GetList called");
            
            LimitationListResponse limitationListResponse = limitationOperationService.getAll();
            
            // Convert to GRPC response
            LimitationListResponseGrpc.Builder limitationListResponseBuilder = LimitationListResponseGrpc.newBuilder();
            if (limitationListResponse != null && limitationListResponse.getLimitationObjectList() != null) {
                for (var limitationObject : limitationListResponse.getLimitationObjectList()) {
                    LimitationObjectGrpc limitationObjectGrpc = LimitationObjectGrpc.newBuilder()
                        .setName(limitationObject.getName() != null ? limitationObject.getName() : "")
                        .setDescription(limitationObject.getDescription() != null ? limitationObject.getDescription() : "")
                        .build();
                    limitationListResponseBuilder.addLimitationObjectList(limitationObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setLimitationListResponse(limitationListResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GetList completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GetList failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GetList unexpected error: {}", e.getMessage(), e);
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