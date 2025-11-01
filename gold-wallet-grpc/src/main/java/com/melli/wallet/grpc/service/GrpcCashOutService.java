package com.melli.wallet.grpc.service;

import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.CashOutOperationService;
import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.request.wallet.cash.CashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.cash.CashOutWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

/**
 * Class Name: GrpcCashOutService
 * Description: GRPC service implementation for cash out operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcCashOutService extends CashOutServiceGrpc.CashOutServiceImplBase {

    private final CashOutOperationService cashOutOperationService;

    @Override
    public void generateUuid(CashGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateUuid called with nationalCode: {}, amount: {}, accountNumber: {}",
                    request.getNationalCode(), request.getAmount(), request.getAccountNumber());

            CashGenerateUuidRequestJson generateUuidRequest = new CashGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setAmount(request.getAmount());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());

            UuidResponse uuid = cashOutOperationService.generateUuid(
                    RequestContext.getChannelEntity(),
                    generateUuidRequest.getNationalCode(),
                    generateUuidRequest.getAmount(),
                    generateUuidRequest.getAccountNumber()
            );

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setUuidResponse(UuidResponseGrpc.newBuilder()
                            .setUuid(uuid.getUniqueIdentifier())
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: GenerateUuid completed successfully with uuid: {}", uuid);

        } catch (InternalServiceException e) {
            log.error("GRPC: GenerateUuid failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: GenerateUuid unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void withdraw(CashOutWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Withdraw called with uniqueIdentifier: {}, nationalCode: {}",
                    request.getUniqueIdentifier(), request.getNationalCode());

            CashOutWalletRequestJson cashOutRequest = new CashOutWalletRequestJson();
            cashOutRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            cashOutRequest.setAmount(request.getAmount());
            cashOutRequest.setIban(request.getIban());
            cashOutRequest.setNationalCode(request.getNationalCode());
            cashOutRequest.setAdditionalData(request.getAdditionalData());
            cashOutRequest.setAccountNumber(request.getAccountNumber());
            cashOutRequest.setSign(request.getSign());

            CashOutResponse cashOutResponse = cashOutOperationService.withdrawal(
                    new CashOutObjectDTO(
                            RequestContext.getChannelEntity(),
                            cashOutRequest.getNationalCode(),
                            cashOutRequest.getUniqueIdentifier(),
                            cashOutRequest.getAmount(),
                            cashOutRequest.getIban(),
                            cashOutRequest.getAccountNumber(),
                            cashOutRequest.getAdditionalData(),
                            RequestContext.getClientIp(),
                            cashOutRequest.getMerchantId()
                    )
            );

            // Convert to GRPC response
            CashOutResponseGrpc cashOutResponseGrpc = CashOutResponseGrpc.newBuilder()
                    .setNationalCode(cashOutResponse.getNationalCode() != null ? cashOutResponse.getNationalCode() : "")
                    .setAvailableBalance(cashOutResponse.getAvailableBalance() != null ? cashOutResponse.getAvailableBalance() : "")
                    .setBalance(cashOutResponse.getBalance() != null ? cashOutResponse.getBalance() : "")
                    .setUniqueIdentifier(cashOutResponse.getUniqueIdentifier() != null ? cashOutResponse.getUniqueIdentifier() : "")
                    .setWalletAccountNumber(cashOutResponse.getWalletAccountNumber() != null ? cashOutResponse.getWalletAccountNumber() : "")
                    .build();

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setCashOutResponse(cashOutResponseGrpc)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: Withdraw completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: Withdraw failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Withdraw unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void inquiryCashOut(InquiryCashOutRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: InquiryCashOut called with uniqueIdentifier: {}", request.getUniqueIdentifier());

            CashOutTrackResponse trackResponse = cashOutOperationService.inquiry(
                    RequestContext.getChannelEntity(),
                    request.getUniqueIdentifier(),
                    RequestContext.getClientIp()
            );

            // Convert to GRPC response
            CashOutTrackResponseGrpc.Builder trackResponseBuilder = CashOutTrackResponseGrpc.newBuilder()
                    .setId(trackResponse.getId())
                    .setNationalCode(trackResponse.getNationalCode() != null ? trackResponse.getNationalCode() : "")
//                    .setIban(trackResponse.getIban() != null ? trackResponse.getIban() : "")
                    .setAmount(String.valueOf(trackResponse.getPrice()))
                    .setUniqueIdentifier(trackResponse.getUniqueIdentifier() != null ? trackResponse.getUniqueIdentifier() : "")
                    .setResult(trackResponse.getResult())
                    .setDescription(trackResponse.getDescription() != null ? trackResponse.getDescription() : "")
                    .setWalletAccountNumber(trackResponse.getWalletAccountNumber() != null ? trackResponse.getWalletAccountNumber() : "")
                    .setCreateTime(trackResponse.getCreateTime() != null ? trackResponse.getCreateTime() : "")
                    .setCreateTimeTimestamp(trackResponse.getCreateTimeTimestamp());

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setCashOutTrackResponse(trackResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: InquiryCashOut completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: InquiryCashOut failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: InquiryCashOut unexpected error: {}", e.getMessage(), e);
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
