package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.CashOutOperationService;
import com.melli.wallet.domain.dto.PhysicalCashOutObjectDTO;
import com.melli.wallet.domain.request.wallet.physical.PhysicalCashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.physical.PhysicalCashOutWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: GrpcPhysicalCashOutService
 * Description: GRPC service implementation for physical cash out operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcPhysicalCashOutService extends PhysicalCashOutServiceGrpc.PhysicalCashOutServiceImplBase {

    private final CashOutOperationService cashOutOperationService;

    @Override
    public void generateUuid(PhysicalCashGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateUuid called with nationalCode: {}, quantity: {}, accountNumber: {}", 
                request.getNationalCode(), request.getQuantity(), request.getAccountNumber());
            
            PhysicalCashGenerateUuidRequestJson generateUuidRequest = new PhysicalCashGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());
            generateUuidRequest.setCurrency(request.getCurrency());
            
            UuidResponse uuid = cashOutOperationService.physicalGenerateUuid(
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
    public void withdrawal(PhysicalCashOutWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Withdrawal called with uniqueIdentifier: {}, nationalCode: {}", 
                request.getUniqueIdentifier(), request.getNationalCode());
            
            PhysicalCashOutWalletRequestJson withdrawalRequest = new PhysicalCashOutWalletRequestJson();
            withdrawalRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            withdrawalRequest.setQuantity(request.getQuantity());
            withdrawalRequest.setNationalCode(request.getNationalCode());
            withdrawalRequest.setAdditionalData(request.getAdditionalData());
            withdrawalRequest.setAccountNumber(request.getAccountNumber());
            withdrawalRequest.setSign(request.getSign());
            withdrawalRequest.setCurrency(request.getCurrency());
            
            // Convert CommissionObjectGrpc to CommissionObject
            CommissionObject commissionObject =
                new CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            withdrawalRequest.setCommissionObject(commissionObject);
            
            PhysicalCashOutResponse withdrawalResponse = cashOutOperationService.physicalWithdrawal(
                new PhysicalCashOutObjectDTO(
                    RequestContext.getChannelEntity(),
                    withdrawalRequest.getNationalCode(),
                    withdrawalRequest.getUniqueIdentifier(),
                    new BigDecimal(withdrawalRequest.getQuantity()),
                    withdrawalRequest.getAccountNumber(),
                    withdrawalRequest.getAdditionalData(),
                    RequestContext.getClientIp(),
                    new BigDecimal(withdrawalRequest.getCommissionObject().getAmount()),
                    withdrawalRequest.getCurrency(),
                    withdrawalRequest.getCommissionObject().getCurrency()
                )
            );
            
            // Convert to GRPC response
            PhysicalCashOutResponseGrpc withdrawalResponseGrpc = PhysicalCashOutResponseGrpc.newBuilder()
                .setNationalCode(withdrawalResponse.getNationalCode() != null ? withdrawalResponse.getNationalCode() : "")
                .setBalance(withdrawalResponse.getBalance() != null ? withdrawalResponse.getBalance() : "")
                .setUniqueIdentifier(withdrawalResponse.getUniqueIdentifier() != null ? withdrawalResponse.getUniqueIdentifier() : "")
                .setWalletAccountNumber(withdrawalResponse.getWalletAccountNumber() != null ? withdrawalResponse.getWalletAccountNumber() : "")
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setPhysicalCashOutResponse(withdrawalResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Withdrawal completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Withdrawal failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Withdrawal unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void inquiry(InquiryPhysicalCashOutRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: InquiryPhysicalCashOut called with uniqueIdentifier: {}", request.getUniqueIdentifier());

            PhysicalCashOutTrackResponse trackResponse = cashOutOperationService.physicalInquiry(
                    RequestContext.getChannelEntity(),
                    request.getUniqueIdentifier(),
                    RequestContext.getClientIp()
            );

            // Convert to GRPC response
            PhysicalCashOutTrackResponseGrpc.Builder trackResponseBuilder = PhysicalCashOutTrackResponseGrpc.newBuilder()
                    .setId(trackResponse.getId())
                    .setNationalCode(trackResponse.getNationalCode() != null ? trackResponse.getNationalCode() : "")
                    .setQuantity(trackResponse.getQuantity() != null ? String.valueOf(trackResponse.getQuantity()) : "")
                    .setUniqueIdentifier(trackResponse.getUniqueIdentifier() != null ? trackResponse.getUniqueIdentifier() : "")
                    .setResult(trackResponse.getResult())
                    .setDescription(trackResponse.getDescription() != null ? trackResponse.getDescription() : "")
                    .setWalletAccountNumber(trackResponse.getWalletAccountNumber() != null ? trackResponse.getWalletAccountNumber() : "")
                    .setCreateTime(trackResponse.getCreateTime() != null ? trackResponse.getCreateTime() : "")
                    .setCreateTimeTimestamp(trackResponse.getCreateTimeTimestamp());

            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setPhysicalCashOutTrackResponse(trackResponseBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GRPC: InquiryPhysicalCashOut completed successfully");

        } catch (InternalServiceException e) {
            log.error("GRPC: InquiryPhysicalCashOut failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: InquiryPhysicalCashOut unexpected error: {}", e.getMessage(), e);
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
