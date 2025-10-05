package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.CashInOperationService;
import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.request.wallet.cash.CashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.cash.CashInWalletRequestJson;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

/**
 * Class Name: GrpcCashInService
 * Description: GRPC service implementation for cash in operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcCashInService extends CashInServiceGrpc.CashInServiceImplBase {

    private final CashInOperationService cashInOperationService;

    @Override
    public void generateUuid(CashGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateUuid called with nationalCode: {}, amount: {}, accountNumber: {}", 
                request.getNationalCode(), request.getAmount(), request.getAccountNumber());
            
            CashGenerateUuidRequestJson generateUuidRequest = new CashGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setAmount(request.getAmount());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());
            
            UuidResponse uuid = cashInOperationService.generateUuid(
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
    public void charge(CashInWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Charge called with uniqueIdentifier: {}, nationalCode: {}", 
                request.getUniqueIdentifier(), request.getNationalCode());
            
            CashInWalletRequestJson cashInRequest = new CashInWalletRequestJson();
            cashInRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            cashInRequest.setReferenceNumber(request.getReferenceNumber());
            cashInRequest.setAmount(request.getAmount());
            cashInRequest.setNationalCode(request.getNationalCode());
            cashInRequest.setAdditionalData(request.getAdditionalData());
            cashInRequest.setCashInType(request.getCashInType());
            cashInRequest.setAccountNumber(request.getAccountNumber());
            cashInRequest.setSign(request.getSign());
            
            CashInResponse cashInResponse = cashInOperationService.charge(
                new ChargeObjectDTO(
                    RequestContext.getChannelEntity(),
                    cashInRequest.getNationalCode(),
                    cashInRequest.getUniqueIdentifier(),
                    cashInRequest.getAmount(),
                    cashInRequest.getReferenceNumber(),
                    cashInRequest.getAccountNumber(),
                    cashInRequest.getAdditionalData(),
                    RequestContext.getClientIp(),
                    cashInRequest.getCashInType()
                )
            );
            
            // Convert to GRPC response
            CashInResponseGrpc cashInResponseGrpc = CashInResponseGrpc.newBuilder()
                .setNationalCode(cashInResponse.getNationalCode() != null ? cashInResponse.getNationalCode() : "")
                .setAvailableBalance(cashInResponse.getAvailableBalance() != null ? cashInResponse.getAvailableBalance() : "")
                .setBalance(cashInResponse.getBalance() != null ? cashInResponse.getBalance() : "")
                .setUniqueIdentifier(cashInResponse.getUniqueIdentifier() != null ? cashInResponse.getUniqueIdentifier() : "")
                .setWalletAccountNumber(cashInResponse.getWalletAccountNumber() != null ? cashInResponse.getWalletAccountNumber() : "")
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCashInResponse(cashInResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Charge completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Charge failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Charge unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void inquiryCashIn(InquiryCashInRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: InquiryCashIn called with uniqueIdentifier: {}", request.getUniqueIdentifier());
            
            CashInTrackResponse trackResponse = cashInOperationService.inquiry(
                RequestContext.getChannelEntity(),
                request.getUniqueIdentifier(),
                RequestContext.getClientIp()
            );
            
            // Convert to GRPC response
            CashInTrackResponseGrpc.Builder trackResponseBuilder = CashInTrackResponseGrpc.newBuilder()
                    .setId(trackResponse.getId())
                    .setNationalCode(trackResponse.getNationalCode() != null ? trackResponse.getNationalCode() : "")
                    .setRefNumber(trackResponse.getRefNumber() != null ? trackResponse.getRefNumber() : "")
                    .setAmount(trackResponse.getAmount())
                    .setUniqueIdentifier(trackResponse.getUniqueIdentifier() != null ? trackResponse.getUniqueIdentifier() : "")
                    .setResult(trackResponse.getResult())
                    .setDescription(trackResponse.getDescription() != null ? trackResponse.getDescription() : "")
                    .setWalletAccountNumber(trackResponse.getWalletAccountNumber() != null ? trackResponse.getWalletAccountNumber() : "")
                    .setCreateTime(trackResponse.getCreateTime() != null ? trackResponse.getCreateTime() : "")
                    .setCreateTimeTimestamp(trackResponse.getCreateTimeTimestamp());


            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCashInTrackResponse(trackResponseBuilder.build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: InquiryCashIn completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: InquiryCashIn failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: InquiryCashIn unexpected error: {}", e.getMessage(), e);
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
