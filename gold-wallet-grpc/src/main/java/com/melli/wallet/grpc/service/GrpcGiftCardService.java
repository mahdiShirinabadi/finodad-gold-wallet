package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.util.DataStringUtil;
import com.melli.wallet.service.operation.GiftCardOperationService;
import com.melli.wallet.domain.dto.GiftCardProcessObjectDTO;
import com.melli.wallet.domain.dto.GiftCardPaymentObjectDTO;
import com.melli.wallet.domain.request.wallet.giftcard.GiftCardGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.giftcard.GiftCardProcessRequestJson;
import com.melli.wallet.domain.request.wallet.giftcard.PaymentGiftCardRequestJson;
import com.melli.wallet.domain.response.giftcard.GiftCardUuidResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.SecurityOperationService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: GrpcGiftCardService
 * Description: GRPC service implementation for gift card operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcGiftCardService extends GiftCardServiceGrpc.GiftCardServiceImplBase {

    private final GiftCardOperationService giftCardOperationService;
    private final SecurityOperationService securityService;

    @Override
    public void generateUuid(GiftCardGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateUuid called with nationalCode: {}, quantity: {}, accountNumber: {}",
                    request.getNationalCode(), request.getQuantity(), request.getAccountNumber());

            GiftCardGenerateUuidRequestJson generateUuidRequest = new GiftCardGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());
            generateUuidRequest.setCurrency(request.getCurrency());

            GiftCardUuidResponse uuid = giftCardOperationService.generateUuid(
                    RequestContext.getChannelEntity(),
                    generateUuidRequest.getNationalCode(),
                    generateUuidRequest.getQuantity(),
                    generateUuidRequest.getAccountNumber(),
                    generateUuidRequest.getCurrency()
            );

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setGiftCardUuidResponse(GiftCardUuidResponseGrpc.newBuilder()
                            .setUuid(uuid.getUniqueIdentifier())
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GenerateUuid completed successfully with uuid: {}", uuid.getUniqueIdentifier());

        } catch (InternalServiceException e) {
            log.error("GRPC: GenerateUuid failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GenerateUuid unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void process(GiftCardProcessRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Process called with uniqueIdentifier: {}, nationalCode: {}",
                    request.getUniqueIdentifier(), request.getNationalCode());

            securityService.checkSign(RequestContext.getChannelEntity(), request.getSign(), DataStringUtil.createGiftCardProcessDataString(request.getUniqueIdentifier(),
                    request.getQuantity(), request.getNationalCode()));

            GiftCardResponse processResponse = giftCardOperationService.process(
                    new GiftCardProcessObjectDTO(
                            RequestContext.getChannelEntity(),
                            request.getUniqueIdentifier(),
                            request.getQuantity(),
                            new BigDecimal(request.getCommissionObject().getAmount()),
                            request.getCommissionObject().getCurrency(),
                            request.getNationalCode(),
                            request.getAccountNumber(),
                            request.getDestinationNationalCode(),
                            RequestContext.getClientIp(),
                            request.getAdditionalData()
                    )
            );

            // Convert to GRPC response
            GiftCardResponseGrpc processResponseGrpc = GiftCardResponseGrpc.newBuilder()
                    .setActiveCode(processResponse.getActiveCode() != null ? processResponse.getActiveCode() : "")
                    .setQuantity(processResponse.getQuantity() != null ? processResponse.getQuantity() : "")
                    .setCurrency(processResponse.getCurrency() != null ? processResponse.getCurrency() : "")
                    .setExpireTime(processResponse.getExpireTime() != null ? processResponse.getExpireTime() : "")
                    .setExpireTimeTimeStamp(processResponse.getExpireTimeTimeStamp())
                    .build();

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setGiftCardResponse(processResponseGrpc)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: Process completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: Process failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Process unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void inquiry(InquiryGiftCardRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Inquiry called with uniqueIdentifier: {}", request.getUniqueIdentifier());

            GiftCardTrackResponse trackResponse = giftCardOperationService.inquiry(
                    RequestContext.getChannelEntity(),
                    request.getUniqueIdentifier(),
                    RequestContext.getClientIp()
            );

            // Convert to GRPC response
            GiftCardTrackResponseGrpc.Builder trackResponseBuilder = GiftCardTrackResponseGrpc.newBuilder()
                    .setId(trackResponse.getId())
                    .setNationalCode(trackResponse.getNationalCode() != null ? trackResponse.getNationalCode() : "")
                    .setActiveCode(trackResponse.getActiveCode() != null ? trackResponse.getActiveCode() : "")
                    .setQuantity(trackResponse.getQuantity() != null ? trackResponse.getQuantity() : "")
                    .setUniqueIdentifier(trackResponse.getUniqueIdentifier() != null ? trackResponse.getUniqueIdentifier() : "")
                    .setStatus(trackResponse.getStatus() != null ? trackResponse.getStatus() : "")
                    .setDescription(trackResponse.getDescription() != null ? trackResponse.getDescription() : "")
                    .setWalletAccountNumber(trackResponse.getWalletAccountNumber() != null ? trackResponse.getWalletAccountNumber() : "")
                    .setDestWalletAccountNumber(trackResponse.getDestWalletAccountNumber() != null ? trackResponse.getDestWalletAccountNumber() : "")
                    .setDestNationalCode(trackResponse.getDestNationalCode() != null ? trackResponse.getDestNationalCode() : "")
                    .setCreateTime(trackResponse.getCreateTime() != null ? trackResponse.getCreateTime() : "")
                    .setCreateTimeTimestamp(trackResponse.getCreateTimeTimestamp())
                    .setExpireTime(trackResponse.getExpireTime() != null ? trackResponse.getExpireTime() : "")
                    .setExpireTimeTimestamp(trackResponse.getCreateTimeTimestamp());

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setGiftCardTrackResponse(trackResponseBuilder.build())
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
    public void payment(PaymentGiftCardRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Payment called with giftCardUniqueCode: {}, nationalCode: {}",
                    request.getGiftCardUniqueCode(), request.getNationalCode());

            PaymentGiftCardRequestJson paymentRequest = new PaymentGiftCardRequestJson();
            paymentRequest.setGiftCardUniqueCode(request.getGiftCardUniqueCode());
            paymentRequest.setQuantity(request.getQuantity());
            paymentRequest.setCurrency(request.getCurrency());
            paymentRequest.setNationalCode(request.getNationalCode());
            paymentRequest.setAccountNumber(request.getAccountNumber());
            paymentRequest.setAdditionalData(request.getAdditionalData());

            giftCardOperationService.payment(
                    new GiftCardPaymentObjectDTO(
                            RequestContext.getChannelEntity(),
                            paymentRequest.getGiftCardUniqueCode(),
                            paymentRequest.getQuantity(),
                            paymentRequest.getCurrency(),
                            paymentRequest.getNationalCode(),
                            RequestContext.getClientIp(),
                            paymentRequest.getAccountNumber(),
                            paymentRequest.getAdditionalData()
                    )
            );

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setEmpty(Empty.newBuilder().build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: Payment completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: Payment failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Payment unexpected error: {}", e.getMessage(), e);
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
