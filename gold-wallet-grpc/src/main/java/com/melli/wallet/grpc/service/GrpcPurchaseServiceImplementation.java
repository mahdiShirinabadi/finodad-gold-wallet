package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.SellRequestDTO;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackObject;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.exception.GrpcErrorHandler;
import com.melli.wallet.service.PurchaseService;
import com.melli.wallet.service.ResourceService;
import com.melli.wallet.service.SecurityService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;


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
    private final GrpcErrorHandler exceptionHandler;

    @PreAuthorize("hasAuthority(\""+ ResourceService.GENERATE_PURCHASE_UNIQUE_IDENTIFIER +"\")")
    @Override
    public void generateBuyUuid(BuyGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("start call buy uuid nationalCode ===> {}", request.getNationalCode());
            String channelIp = RequestContext.getClientIp();
            UuidResponse response = purchaseService.buyGenerateUuid(
                    new BuyRequestDTO(
                            RequestContext.getChannelEntity(),
                            "",
                            new BigDecimal(request.getQuantity()),
                            Long.parseLong(request.getPrice()),
                            request.getAccountNumber(),
                            "",
                            request.getMerchantId(),
                            request.getNationalCode(),
                            null,
                            request.getCurrency(),
                            channelIp,
                            "",""
                    )
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setUuidResponse(convertToGrpcUuidResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "PurchaseService/generateBuyUuid");
        }
    }

    @Override
    @PreAuthorize("hasAuthority(\""+ ResourceService.GENERATE_PURCHASE_UNIQUE_IDENTIFIER +"\")")
    public void generateSellUuid(SellGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("start call sell uuid nationalCode ===> {}", request.getNationalCode());
            UuidResponse response = purchaseService.sellGenerateUuid(
                    RequestContext.getChannelEntity(),
                    request.getNationalCode(),
                    request.getQuantity(),
                    request.getAccountNumber(),
                    request.getCurrency()
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setUuidResponse(convertToGrpcUuidResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "PurchaseService/GenerateSellUuid");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceService.BUY +"\")")
    @Override
    public void inquiry(PurchaseTrackRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start call purchase in username ===> {}, uniqueIdentifier ===> {}, from ip ===> {}",
                    username, request.getUniqueIdentifier(), channelIp);

            PurchaseTrackResponse response = purchaseService.purchaseTrack(
                    RequestContext.getChannelEntity(),
                    request.getUniqueIdentifier(),
                    request.getType(),
                    channelIp
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setPurchaseTrackResponse(convertToGrpcPurchaseTrackResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "PurchaseService/PurchaseTrack");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceService.BUY +"\")")
    @Override
    public void buy(BuyWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start call buy in username ===> {}, nationalCode ===> {}, from ip ===> {}",
                    username, request.getNationalCode(), channelIp);

            securityService.checkSign(
                    RequestContext.getChannelEntity(),
                    request.getSign(),
                    request.getUniqueIdentifier() + "|" + request.getMerchantId() + "|" + request.getTotalPrice() + "|" + request.getNationalCode()
            );

            PurchaseResponse response = purchaseService.buy(
                    new BuyRequestDTO(
                            RequestContext.getChannelEntity(),
                            request.getUniqueIdentifier(),
                            new BigDecimal(request.getQuantity()),
                            Long.parseLong(request.getTotalPrice()),
                            request.getWalletAccountNumber(),
                            request.getAdditionalData(),
                            request.getMerchantId(),
                            request.getNationalCode(),
                            new BigDecimal(request.getCommissionObject().getAmount()),
                            request.getCurrency(),
                            channelIp,
                            "",request.getCommissionObject().getCurrency()
                    )
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setPurchaseResponse(convertToGrpcPurchaseResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "PurchaseService/Sell");
        }
    }

    @Override
    @PreAuthorize("hasAuthority(\""+ ResourceService.BUY_DIRECT +"\")")
    public void buyDirect(BuyDirectWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start call buy direct in username ===> {}, nationalCode ===> {}, from ip ===> {}",
                    username, request.getNationalCode(), channelIp);

            securityService.checkSign(
                    RequestContext.getChannelEntity(),
                    request.getSign(),
                    request.getUniqueIdentifier() + "|" + request.getMerchantId() + "|" + request.getTotalPrice() + "|" + request.getNationalCode()
            );

            PurchaseResponse response = purchaseService.buyDirect(
                    new BuyRequestDTO(
                            RequestContext.getChannelEntity(),
                            request.getUniqueIdentifier(),
                            new BigDecimal(request.getQuantity()),
                            Long.parseLong(request.getTotalPrice()),
                            request.getWalletAccountNumber(),
                            request.getAdditionalData(),
                            request.getMerchantId(),
                            request.getNationalCode(),
                            new BigDecimal(request.getCommissionObject().getAmount()),
                            request.getCurrency(),
                            channelIp,
                            request.getRefNumber(),
                            request.getCommissionObject().getCurrency()
                    )
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setPurchaseResponse(convertToGrpcPurchaseResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "PurchaseService/BuyDirect");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceService.SELL +"\")")
    @Override
    public void sell(SellWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            log.info("start call sell in username ===> {}, nationalCode ===> {}, from ip ===> {}",
                    username, request.getNationalCode(), channelIp);

            securityService.checkSign(
                    RequestContext.getChannelEntity(),
                    request.getSign(),
                    request.getUniqueIdentifier() + "|" + request.getMerchantId() + "|" + request.getQuantity() + "|" + request.getNationalCode()
            );

            PurchaseResponse response = purchaseService.sell(
                    new SellRequestDTO(
                            RequestContext.getChannelEntity(),
                            request.getUniqueIdentifier(),
                            new BigDecimal(request.getQuantity()),
                            Long.parseLong(request.getPrice()),
                            request.getWalletAccountNumber(),
                            request.getAdditionalData(),
                            request.getMerchantId(),
                            request.getNationalCode(),
                            new BigDecimal(request.getCommissionObject().getAmount()),
                            request.getCurrency(),
                            channelIp,
                            request.getCommissionObject().getCurrency()
                    )
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setPurchaseResponse(convertToGrpcPurchaseResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "PurchaseService/Sell");
        }
    }

    private UuidResponseGrpc convertToGrpcUuidResponse(UuidResponse response) {
        return UuidResponseGrpc.newBuilder()
                .setUuid(response.getUniqueIdentifier() != null ? response.getUniqueIdentifier() : "")
                .build();
    }

    private PurchaseResponseGrpc convertToGrpcPurchaseResponse(PurchaseResponse response) {
        PurchaseResponseGrpc.Builder builder = PurchaseResponseGrpc.newBuilder()
                .setNationalCode(response.getNationalCode() != null ? response.getNationalCode() : "")
                .setAmount(response.getAmount() != null ? response.getAmount() : "")
                .setPrice(response.getPrice() != null ? response.getPrice() : "")
                .setUniqueIdentifier(response.getUniqueIdentifier() != null ? response.getUniqueIdentifier() : "")
                .setType(response.getType() != null ? response.getType() : "")
                .setChannelName(response.getChannelName() != null ? response.getChannelName() : "")
                .setCreateTime(response.getCreateTime() != null ? response.getCreateTime() : "")
                .setCreateTimeTimestamp(response.getCreateTimeTimestamp() != null ? response.getCreateTimeTimestamp() : 0);
        return builder.build();
    }

    private PurchaseTrackResponseGrpc convertToGrpcPurchaseTrackResponse(PurchaseTrackResponse response) {
        PurchaseTrackResponseGrpc.Builder builder = PurchaseTrackResponseGrpc.newBuilder();
        if (response.getPurchaseTrackObjectList() != null) {
            builder.addAllPurchaseTrackObjectList(
                    response.getPurchaseTrackObjectList().stream()
                            .map(this::convertToGrpcPurchaseTrackObject)
                            .toList()
            );
        }
        return builder.build();
    }

    private PurchaseTrackObjectGrpc convertToGrpcPurchaseTrackObject(PurchaseTrackObject trackObject) {
        PurchaseTrackObjectGrpc.Builder builder = PurchaseTrackObjectGrpc.newBuilder()
                .setNationalCode(trackObject.getNationalCode() != null ? trackObject.getNationalCode() : "")
                .setAmount(trackObject.getAmount() != null ? trackObject.getAmount() : "")
                .setPrice(trackObject.getPrice() != null ? trackObject.getPrice() : "")
                .setUniqueIdentifier(trackObject.getUniqueIdentifier() != null ? trackObject.getUniqueIdentifier() : "")
                .setType(trackObject.getType() != null ? trackObject.getType() : "")
                .setAccountNumber(trackObject.getAccountNumber() != null ? trackObject.getAccountNumber() : "")
                .setResult(trackObject.getResult() != null ? trackObject.getResult() : "")
                .setDescription(trackObject.getDescription() != null ? trackObject.getDescription() : "")
                .setChannelName(trackObject.getChannelName() != null ? trackObject.getChannelName() : "")
                .setCreateTime(trackObject.getCreateTime() != null ? trackObject.getCreateTime() : "")
                .setCreateTimeTimestamp(trackObject.getCreateTimeTimestamp() != null ? trackObject.getCreateTimeTimestamp() : 0);
        return builder.build();
    }


}
