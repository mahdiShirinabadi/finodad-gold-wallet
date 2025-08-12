package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.response.purchase.MerchantObject;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.exception.GrpcErrorHandler;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Class Name: GrpcMerchantService
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcMerchantService extends MerchantServiceGrpc.MerchantServiceImplBase {

    private final MerchantRepositoryService merchantRepositoryService;
    private final GrpcErrorHandler exceptionHandler;

    @Override
    public void getMerchant(GetMerchantRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start call getMerchant in username ===> {}, currency ===> {}, from ip ===> {}",
                    username, request.getCurrency(), channelIp);

            // Validate currency (placeholder for @StringValidation)
            if (!isValidCurrency(request.getCurrency())) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid currency").asRuntimeException());
                return;
            }

            MerchantResponse response = merchantRepositoryService.getMerchant(
                    RequestContext.getChannelEntity(),
                    request.getCurrency()
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setMerchantResponseGrpc(convertToGrpcMerchantResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "GrpcMerchantService/GetMerchant");
        }
    }

    private MerchantResponseGrpc convertToGrpcMerchantResponse(MerchantResponse response) {
        MerchantResponseGrpc.Builder builder = MerchantResponseGrpc.newBuilder();
        if (response.getMerchantObjectList() != null) {
            builder.addAllMerchantObjectList(
                    response.getMerchantObjectList().stream()
                            .map(this::convertToGrpcMerchantObject)
                            .toList()
            );
        }
        return builder.build();
    }

    private MerchantObjectGrpc convertToGrpcMerchantObject(MerchantObject merchantObject) {
        MerchantObjectGrpc.Builder builder = MerchantObjectGrpc.newBuilder()
                .setId(merchantObject.getId())
                .setName(merchantObject.getName())
                .setLogo(merchantObject.getLogo());
        return builder.build();
    }

    // Placeholder for currency validation
    private boolean isValidCurrency(String currency) {
        return currency != null && !currency.trim().isEmpty();
    }

}
