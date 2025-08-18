package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.response.limitation.LimitationListResponse;
import com.melli.wallet.domain.response.limitation.LimitationObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.exception.GrpcErrorHandler;
import com.melli.wallet.service.operation.LimitationOperationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Class Name: GrpcLimitationService
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcLimitationService extends LimitationServiceGrpc.LimitationServiceImplBase {

    private final LimitationOperationService limitationOperationService;
    private final GrpcErrorHandler exceptionHandler;

    @Override
    public void getLimitationValue(GetLimitationValueRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start get setting value in username ===> {}, limitationName ===> {}, accountNumber ===> {}, from ip ===> {}",
                    username, request.getLimitationName(), request.getAccountNumber(), channelIp);

            // Validate parameters (placeholder for @Valid)
            if (!isValidInput(request.getLimitationName(), request.getAccountNumber(), request.getNationalCode())) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid input parameters").asRuntimeException());
                return;
            }

            String value = limitationOperationService.getValue(
                    RequestContext.getChannelEntity(),
                    request.getLimitationName(),
                    request.getAccountNumber(),
                    request.getNationalCode(),
                    channelIp
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setLimitationCustomResponseGrpc(LimitationCustomResponseGrpc.newBuilder().setValue(value != null ? value : "").build())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "LimitationGrpcService/GetLimitationValue");
        }
    }

    @Override
    public void getLimitationList(GetLimitationListRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start call cashIn in username ===> {}, from ip ===> {}", username, channelIp);

            LimitationListResponse response = limitationOperationService.getAll();

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setLimitationListResponseGrpc(convertToGrpcLimitationListResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "LimitationGrpcService/GetLimitationList");
        }
    }

    private LimitationListResponseGrpc convertToGrpcLimitationListResponse(LimitationListResponse response) {
        LimitationListResponseGrpc.Builder builder = LimitationListResponseGrpc.newBuilder();
        if (response.getLimitationObjectList() != null) {
            builder.addAllLimitationObjectList(
                    response.getLimitationObjectList().stream()
                            .map(this::convertToGrpcLimitationObject)
                            .toList()
            );
        }
        return builder.build();
    }

    private LimitationObjectGrpc convertToGrpcLimitationObject(LimitationObject limitationObject) {
        LimitationObjectGrpc.Builder builder = LimitationObjectGrpc.newBuilder()
                .setName(limitationObject.getName() != null ? limitationObject.getName() : "")
                .setDescription(limitationObject.getDescription() != null ? limitationObject.getDescription() : "");
        return builder.build();
    }

    // Placeholder for input validation
    private boolean isValidInput(String limitationName, String accountNumber, String nationalCode) {
        // Replace with actual validation logic for @Valid
        return limitationName != null && !limitationName.trim().isEmpty() &&
                accountNumber != null && !accountNumber.trim().isEmpty() &&
                nationalCode != null && !nationalCode.trim().isEmpty();
    }

}
