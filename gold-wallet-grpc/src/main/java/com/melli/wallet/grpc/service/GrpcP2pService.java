package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.Person2PersonOperationService;
import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.request.wallet.p2p.P2pGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.p2p.P2pRequestJson;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: GrpcP2pService
 * Description: GRPC service implementation for P2P operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcP2pService extends P2pServiceGrpc.P2pServiceImplBase {

    private final Person2PersonOperationService person2PersonOperationService;

    @Override
    public void generateUuid(P2pGenerateUuidRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateUuid called with nationalCode: {}, quantity: {}, accountNumber: {}", 
                request.getNationalCode(), request.getQuantity(), request.getAccountNumber());
            
            P2pGenerateUuidRequestJson generateUuidRequest = new P2pGenerateUuidRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setAccountNumber(request.getAccountNumber());
            generateUuidRequest.setDestAccountNumber(request.getDestAccountNumber());

            P2pUuidResponse uuid = person2PersonOperationService.generateUuid(
                RequestContext.getChannelEntity(),
                generateUuidRequest.getNationalCode(),
                generateUuidRequest.getQuantity(),
                generateUuidRequest.getAccountNumber(),
                generateUuidRequest.getDestAccountNumber()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setP2PUuidResponse(P2pUuidResponseGrpc.newBuilder()
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
    public void process(P2pRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Process called with uniqueIdentifier: {}, nationalCode: {}", 
                request.getUniqueIdentifier(), request.getNationalCode());
            
            P2pRequestJson p2pRequest = new P2pRequestJson();
            p2pRequest.setQuantity(request.getQuantity());
            p2pRequest.setNationalCode(request.getNationalCode());
            p2pRequest.setAccountNumber(request.getAccountNumber());
            p2pRequest.setDestAccountNumber(request.getDestAccountNumber());
            p2pRequest.setAdditionalData(request.getAdditionalData());
            p2pRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            p2pRequest.setSign(request.getSign());
            p2pRequest.setCurrency(request.getCurrency());
            
            // Convert CommissionObjectGrpc to CommissionObject
            CommissionObject commissionObject =
                new CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            p2pRequest.setCommissionObject(commissionObject);
            
            person2PersonOperationService.process(
                new P2pObjectDTO(
                    RequestContext.getChannelEntity(),
                    p2pRequest.getNationalCode(),
                    p2pRequest.getUniqueIdentifier(),
                    p2pRequest.getAccountNumber(),
                    new BigDecimal(p2pRequest.getQuantity()),
                    p2pRequest.getDestAccountNumber(),
                    p2pRequest.getAdditionalData(),
                    RequestContext.getClientIp(),
                    p2pRequest.getCurrency(),
                    new BigDecimal(p2pRequest.getCommissionObject().getAmount()),
                    p2pRequest.getCommissionObject().getCurrency()
                )
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
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
    public void inquiry(InquiryP2pRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Inquiry called with uniqueIdentifier: {}", request.getUniqueIdentifier());
            
            P2pTrackResponse trackResponse = person2PersonOperationService.inquiry(
                RequestContext.getChannelEntity(),
                request.getUniqueIdentifier(),
                RequestContext.getClientIp()
            );
            
            // Convert to GRPC response
            P2pTrackResponseGrpc.Builder trackResponseBuilder = P2pTrackResponseGrpc.newBuilder()
                    .setId(trackResponse.getId())
                    .setNationalCode(trackResponse.getNationalCode() != null ? trackResponse.getNationalCode() : "")
                    .setAccountNumber(trackResponse.getWalletAccountNumber() != null ? trackResponse.getWalletAccountNumber() : "")
                    .setDestAccountNumber(trackResponse.getDestWalletAccountNumber() != null ? trackResponse.getDestWalletAccountNumber() : "")
                    .setQuantity(trackResponse.getQuantity() != null ? trackResponse.getQuantity() : "")
                    .setUniqueIdentifier(trackResponse.getUniqueIdentifier() != null ? trackResponse.getUniqueIdentifier() : "")
                    .setResult(trackResponse.getResult())
                    .setDescription(trackResponse.getDescription() != null ? trackResponse.getDescription() : "")
                    .setCreateTime(trackResponse.getCreateTime() != null ? trackResponse.getCreateTime() : "")
                    .setCreateTimeTimestamp(trackResponse.getCreateTimeTimestamp());
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setP2PTrackResponse(trackResponseBuilder.build())
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
