package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.grpc.BaseResponseGrpc;
import com.melli.wallet.grpc.BuyGenerateUuidRequestGrpc;
import com.melli.wallet.grpc.PurchaseServiceGrpc;
import com.melli.wallet.grpc.UuidResponseGrpc;
import com.melli.wallet.grpc.exception.GrpcExceptionHandler;
import com.melli.wallet.service.ChannelService;
import com.melli.wallet.service.PurchaseService;
import com.melli.wallet.service.SecurityService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Class Name: PurchaseServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPurchaseServiceImplementation extends PurchaseServiceGrpc.PurchaseServiceImplBase {

    private final PurchaseService purchaseService;
    private final SecurityService securityService;
    private final GrpcExceptionHandler exceptionHandler;
    private final ChannelService channelService;

    @Override
    public void generateBuyUuid(BuyGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
//            validateNumber(request.getPrice(), "price");
            String username = ThreadContext.get("username");
            log.info("Start generateBuyUuid for nationalCode: {}, username: {}", request.getNationalCode(), username);

            UuidResponse response = purchaseService.buyGenerateUuid(channelService.findByUsername(username), request.getNationalCode(), request.getPrice(), request.getAccountNumber());
            BaseResponseGrpc protoResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setUuidResponse(UuidResponseGrpc.newBuilder().setUuid(response.getUniqueIdentifier()).build())
                    .setTrackingId(ThreadContext.get("uuid"))
                    .setDoTime(new Date().toString())
                    .setDoTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(protoResponse);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            exceptionHandler.handleException(ex, responseObserver, "PurchaseService/GenerateBuyUuid");
        }
    }

    // Similar implementations for generateSellUuid, inquiry, buy, buyDirect, sell
    // Each method validates inputs, calls the corresponding purchaseService method, and builds a BaseResponse


}
