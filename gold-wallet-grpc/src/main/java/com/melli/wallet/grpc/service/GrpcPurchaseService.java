package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.PurchaseOperationService;
import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.SellRequestDTO;
import com.melli.wallet.domain.request.wallet.purchase.BuyGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.purchase.SellGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.purchase.BuyWalletRequestJson;
import com.melli.wallet.domain.request.wallet.purchase.BuyDirectWalletRequestJson;
import com.melli.wallet.domain.request.wallet.purchase.SellWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackObject;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: GrpcPurchaseService
 * Description: GRPC service implementation for purchase operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPurchaseService extends PurchaseServiceGrpc.PurchaseServiceImplBase {

    private final PurchaseOperationService purchaseOperationService;

    @Override
    public void generateBuyUuid(BuyGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateBuyUuid called with nationalCode: {}, quantity: {}, price: {}", 
                request.getNationalCode(), request.getQuantity(), request.getPrice());
            
            BuyGenerateUuidRequestJson generateUuidRequest = new BuyGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setPrice(request.getPrice());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());
            generateUuidRequest.setMerchantId(request.getMerchantId());
            generateUuidRequest.setCurrency(request.getCurrency());
            
            UuidResponse uuid = purchaseOperationService.buyGenerateUuid(
                new BuyRequestDTO(
                    RequestContext.getChannelEntity(),
                    "",
                    new BigDecimal(generateUuidRequest.getQuantity()),
                    Long.parseLong(generateUuidRequest.getPrice()),
                    generateUuidRequest.getAccountNumber(),
                    "",
                    generateUuidRequest.getMerchantId(),
                    generateUuidRequest.getNationalCode(),
                    null,
                    generateUuidRequest.getCurrency(),
                    RequestContext.getClientIp(),
                    "",
                    ""
                )
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid(uuid.getUniqueIdentifier())
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GenerateBuyUuid completed successfully with uuid: {}", uuid.getUniqueIdentifier());
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GenerateBuyUuid failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GenerateBuyUuid unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void generateSellUuid(SellGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateSellUuid called with nationalCode: {}, quantity: {}, accountNumber: {}", 
                request.getNationalCode(), request.getQuantity(), request.getAccountNumber());
            
            SellGenerateUuidRequestJson generateUuidRequest = new SellGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());
            generateUuidRequest.setCurrency(request.getCurrency());
            
            UuidResponse uuid = purchaseOperationService.sellGenerateUuid(
                RequestContext.getChannelEntity(),
                generateUuidRequest.getNationalCode(),
                generateUuidRequest.getQuantity(),
                generateUuidRequest.getAccountNumber(),
                generateUuidRequest.getCurrency()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid(uuid.getUniqueIdentifier())
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: GenerateSellUuid completed successfully with uuid: {}", uuid.getUniqueIdentifier());
            
        } catch (InternalServiceException e) {
            log.error("GRPC: GenerateSellUuid failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GenerateSellUuid unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void inquiry(InquiryPurchaseRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Inquiry called with uniqueIdentifier: {}, type: {}", 
                request.getUniqueIdentifier(), request.getType());
            
            PurchaseTrackResponse trackResponse = purchaseOperationService.purchaseTrack(
                RequestContext.getChannelEntity(),
                request.getUniqueIdentifier(),
                request.getType(),
                RequestContext.getClientIp()
            );
            
            // Convert to GRPC response
            PurchaseTrackResponseGrpc.Builder trackResponseBuilder = PurchaseTrackResponseGrpc.newBuilder();
            if (trackResponse != null && trackResponse.getPurchaseTrackObjectList() != null) {
                for (var trackObject : trackResponse.getPurchaseTrackObjectList()) {
                    PurchaseTrackObjectGrpc trackObjectGrpc = PurchaseTrackObjectGrpc.newBuilder()
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
                        .setCreateTimeTimestamp(trackObject.getCreateTimeTimestamp())
                        .build();
                    trackResponseBuilder.addPurchaseTrackObjectList(trackObjectGrpc);
                }
            }
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setPurchaseTrackResponse(trackResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Inquiry completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Inquiry failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Inquiry unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void buy(BuyWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Buy called with uniqueIdentifier: {}, nationalCode: {}", 
                request.getUniqueIdentifier(), request.getNationalCode());
            
            BuyWalletRequestJson buyRequest = new BuyWalletRequestJson();
            buyRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            buyRequest.setQuantity(request.getQuantity());
            buyRequest.setPrice(request.getPrice());
            buyRequest.setWalletAccountNumber(request.getWalletAccountNumber());
            buyRequest.setAdditionalData(request.getAdditionalData());
            buyRequest.setMerchantId(request.getMerchantId());
            buyRequest.setNationalCode(request.getNationalCode());
            buyRequest.setCurrency(request.getCurrency());
            buyRequest.setSign(request.getSign());
            
            // Convert CommissionObjectGrpc to CommissionObject
            com.melli.wallet.domain.response.purchase.CommissionObject commissionObject = 
                new com.melli.wallet.domain.response.purchase.CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            buyRequest.setCommissionObject(commissionObject);
            
            PurchaseResponse buyResponse = purchaseOperationService.buy(
                new BuyRequestDTO(
                    RequestContext.getChannelEntity(),
                    buyRequest.getUniqueIdentifier(),
                    new BigDecimal(buyRequest.getQuantity()),
                    Long.parseLong(buyRequest.getPrice()),
                    buyRequest.getWalletAccountNumber(),
                    buyRequest.getAdditionalData(),
                    buyRequest.getMerchantId(),
                    buyRequest.getNationalCode(),
                    new BigDecimal(buyRequest.getCommissionObject().getAmount()),
                    buyRequest.getCurrency(),
                    RequestContext.getClientIp(),
                    "",
                    buyRequest.getCommissionObject().getCurrency()
                )
            );
            
            // Convert to GRPC response
            PurchaseResponseGrpc buyResponseGrpc = PurchaseResponseGrpc.newBuilder()
                .setNationalCode(buyResponse.getNationalCode() != null ? buyResponse.getNationalCode() : "")
                .setAmount(buyResponse.getAmount() != null ? buyResponse.getAmount() : "")
                .setPrice(buyResponse.getPrice() != null ? buyResponse.getPrice() : "")
                .setUniqueIdentifier(buyResponse.getUniqueIdentifier() != null ? buyResponse.getUniqueIdentifier() : "")
                .setType(buyResponse.getType() != null ? buyResponse.getType() : "")
                .setChannelName(buyResponse.getChannelName() != null ? buyResponse.getChannelName() : "")
                .setCreateTime(buyResponse.getCreateTime() != null ? buyResponse.getCreateTime() : "")
                .setCreateTimeTimestamp(buyResponse.getCreateTimeTimestamp())
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setPurchaseResponse(buyResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Buy completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Buy failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Buy unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void buyDirect(BuyDirectWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: BuyDirect called with uniqueIdentifier: {}, nationalCode: {}", 
                request.getUniqueIdentifier(), request.getNationalCode());
            
            BuyDirectWalletRequestJson buyDirectRequest = new BuyDirectWalletRequestJson();
            buyDirectRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            buyDirectRequest.setQuantity(request.getQuantity());
            buyDirectRequest.setPrice(request.getPrice());
            buyDirectRequest.setWalletAccountNumber(request.getWalletAccountNumber());
            buyDirectRequest.setAdditionalData(request.getAdditionalData());
            buyDirectRequest.setMerchantId(request.getMerchantId());
            buyDirectRequest.setNationalCode(request.getNationalCode());
            buyDirectRequest.setCurrency(request.getCurrency());
            buyDirectRequest.setSign(request.getSign());
            buyDirectRequest.setRefNumber(request.getRefNumber());
            
            // Convert CommissionObjectGrpc to CommissionObject
            com.melli.wallet.domain.response.purchase.CommissionObject commissionObject = 
                new com.melli.wallet.domain.response.purchase.CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            buyDirectRequest.setCommissionObject(commissionObject);
            
            PurchaseResponse buyDirectResponse = purchaseOperationService.buyDirect(
                new BuyRequestDTO(
                    RequestContext.getChannelEntity(),
                    buyDirectRequest.getUniqueIdentifier(),
                    new BigDecimal(buyDirectRequest.getQuantity()),
                    Long.parseLong(buyDirectRequest.getPrice()),
                    buyDirectRequest.getWalletAccountNumber(),
                    buyDirectRequest.getAdditionalData(),
                    buyDirectRequest.getMerchantId(),
                    buyDirectRequest.getNationalCode(),
                    new BigDecimal(buyDirectRequest.getCommissionObject().getAmount()),
                    buyDirectRequest.getCurrency(),
                    RequestContext.getClientIp(),
                    buyDirectRequest.getRefNumber(),
                    buyDirectRequest.getCommissionObject().getCurrency()
                )
            );
            
            // Convert to GRPC response
            PurchaseResponseGrpc buyDirectResponseGrpc = PurchaseResponseGrpc.newBuilder()
                .setNationalCode(buyDirectResponse.getNationalCode() != null ? buyDirectResponse.getNationalCode() : "")
                .setAmount(buyDirectResponse.getAmount() != null ? buyDirectResponse.getAmount() : "")
                .setPrice(buyDirectResponse.getPrice() != null ? buyDirectResponse.getPrice() : "")
                .setUniqueIdentifier(buyDirectResponse.getUniqueIdentifier() != null ? buyDirectResponse.getUniqueIdentifier() : "")
                .setType(buyDirectResponse.getType() != null ? buyDirectResponse.getType() : "")
                .setChannelName(buyDirectResponse.getChannelName() != null ? buyDirectResponse.getChannelName() : "")
                .setCreateTime(buyDirectResponse.getCreateTime() != null ? buyDirectResponse.getCreateTime() : "")
                .setCreateTimeTimestamp(buyDirectResponse.getCreateTimeTimestamp())
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setPurchaseResponse(buyDirectResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: BuyDirect completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: BuyDirect failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: BuyDirect unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void sell(SellWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Sell called with uniqueIdentifier: {}, nationalCode: {}", 
                request.getUniqueIdentifier(), request.getNationalCode());
            
            SellWalletRequestJson sellRequest = new SellWalletRequestJson();
            sellRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            sellRequest.setQuantity(request.getQuantity());
            sellRequest.setPrice(request.getPrice());
            sellRequest.setWalletAccountNumber(request.getWalletAccountNumber());
            sellRequest.setAdditionalData(request.getAdditionalData());
            sellRequest.setMerchantId(request.getMerchantId());
            sellRequest.setNationalCode(request.getNationalCode());
            sellRequest.setCurrency(request.getCurrency());
            sellRequest.setSign(request.getSign());
            
            // Convert CommissionObjectGrpc to CommissionObject
            com.melli.wallet.domain.response.purchase.CommissionObject commissionObject = 
                new com.melli.wallet.domain.response.purchase.CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            sellRequest.setCommissionObject(commissionObject);
            
            PurchaseResponse sellResponse = purchaseOperationService.sell(
                new SellRequestDTO(
                    RequestContext.getChannelEntity(),
                    sellRequest.getUniqueIdentifier(),
                    new BigDecimal(sellRequest.getQuantity()),
                    Long.parseLong(sellRequest.getPrice()),
                    sellRequest.getWalletAccountNumber(),
                    sellRequest.getAdditionalData(),
                    sellRequest.getMerchantId(),
                    sellRequest.getNationalCode(),
                    new BigDecimal(sellRequest.getCommissionObject().getAmount()),
                    sellRequest.getCurrency(),
                    RequestContext.getClientIp(),
                    sellRequest.getCommissionObject().getCurrency()
                )
            );
            
            // Convert to GRPC response
            PurchaseResponseGrpc sellResponseGrpc = PurchaseResponseGrpc.newBuilder()
                .setNationalCode(sellResponse.getNationalCode() != null ? sellResponse.getNationalCode() : "")
                .setAmount(sellResponse.getAmount() != null ? sellResponse.getAmount() : "")
                .setPrice(sellResponse.getPrice() != null ? sellResponse.getPrice() : "")
                .setUniqueIdentifier(sellResponse.getUniqueIdentifier() != null ? sellResponse.getUniqueIdentifier() : "")
                .setType(sellResponse.getType() != null ? sellResponse.getType() : "")
                .setChannelName(sellResponse.getChannelName() != null ? sellResponse.getChannelName() : "")
                .setCreateTime(sellResponse.getCreateTime() != null ? sellResponse.getCreateTime() : "")
                .setCreateTimeTimestamp(sellResponse.getCreateTimeTimestamp())
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setPurchaseResponse(sellResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Sell completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Sell failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Sell unexpected error: {}", e.getMessage(), e);
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
