package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.operation.CollateralOperationService;
import com.melli.wallet.domain.dto.CreateCollateralObjectDTO;
import com.melli.wallet.domain.dto.ReleaseCollateralObjectDTO;
import com.melli.wallet.domain.dto.IncreaseCollateralObjectDTO;
import com.melli.wallet.domain.request.wallet.collateral.UniqueIdentifierCollateralRequestJson;
import com.melli.wallet.domain.request.wallet.collateral.CreateCollateralRequestJson;
import com.melli.wallet.domain.request.wallet.collateral.ReleaseCollateralRequestJson;
import com.melli.wallet.domain.request.wallet.collateral.IncreaseCollateralRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.domain.response.collateral.CollateralTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Class Name: GrpcCollateralService
 * Description: GRPC service implementation for collateral operations
 */
@Service
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcCollateralService extends CollateralServiceGrpc.CollateralServiceImplBase {

    private final CollateralOperationService collateralOperationService;

    @Override
    public void generateUuid(UniqueIdentifierCollateralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: GenerateUuid called with nationalCode: {}, quantity: {}, currency: {}", 
                request.getNationalCode(), request.getQuantity(), request.getCurrency());
            
            UniqueIdentifierCollateralRequestJson generateUuidRequest = new UniqueIdentifierCollateralRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setCurrency(request.getCurrency());
            generateUuidRequest.setWalletAccountNumber(request.getWalletAccountNumber());
            
            UuidResponse uuid = collateralOperationService.generateUniqueIdentifier(
                RequestContext.getChannelEntity(),
                generateUuidRequest.getNationalCode(),
                generateUuidRequest.getQuantity(),
                generateUuidRequest.getCurrency(),
                generateUuidRequest.getWalletAccountNumber()
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
    public void create(CreateCollateralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Create called with uniqueIdentifier: {}, accountNumber: {}", 
                request.getUniqueIdentifier(), request.getAccountNumber());
            
            CreateCollateralRequestJson createRequest = new CreateCollateralRequestJson();
            createRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            createRequest.setCollateralId(request.getCollateralId());
            createRequest.setQuantity(request.getQuantity());
            createRequest.setAccountNumber(request.getAccountNumber());
            createRequest.setDescription(request.getDescription());
            createRequest.setSign(request.getSign());
            
            // Convert CommissionObjectGrpc to CommissionObject
            CommissionObject commissionObject =
                new CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            createRequest.setCommissionObject(commissionObject);
            
            CreateCollateralResponse createResponse = collateralOperationService.create(
                new CreateCollateralObjectDTO(
                    RequestContext.getChannelEntity(),
                    createRequest.getUniqueIdentifier(),
                    new BigDecimal(createRequest.getQuantity()),
                    createRequest.getAccountNumber(),
                    createRequest.getDescription(),
                    new BigDecimal(createRequest.getCommissionObject().getAmount()),
                    createRequest.getCommissionObject().getCurrency(),
                    RequestContext.getClientIp(),
                    createRequest.getCollateralId()
                )
            );
            
            // Convert to GRPC response
            CreateCollateralResponseGrpc createResponseGrpc = CreateCollateralResponseGrpc.newBuilder()
                .setCollateralCode(createResponse.getCollateralCode() != null ? createResponse.getCollateralCode() : "")
                .setQuantity(createResponse.getQuantity() != null ? createResponse.getQuantity() : "")
                .setNationalCode(createResponse.getNationalCode() != null ? createResponse.getNationalCode() : "")
                .build();
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCreateCollateralResponse(createResponseGrpc)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Create completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Create failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Create unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void release(ReleaseCollateralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Release called with collateralCode: {}, nationalCode: {}", 
                request.getCollateralCode(), request.getNationalCode());
            
            ReleaseCollateralRequestJson releaseRequest = new ReleaseCollateralRequestJson();
            releaseRequest.setCollateralCode(request.getCollateralCode());
            releaseRequest.setQuantity(request.getQuantity());
            releaseRequest.setNationalCode(request.getNationalCode());
            releaseRequest.setAdditionalData(request.getAdditionalData());
            releaseRequest.setSign(request.getSign());
            
            // Convert CommissionObjectGrpc to CommissionObject
            CommissionObject commissionObject =
                new CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            releaseRequest.setCommissionObject(commissionObject);
            
            collateralOperationService.release(
                new ReleaseCollateralObjectDTO(
                    RequestContext.getChannelEntity(),
                    releaseRequest.getCollateralCode(),
                    new BigDecimal(releaseRequest.getQuantity()),
                    releaseRequest.getNationalCode(),
                    releaseRequest.getAdditionalData(),
                    new BigDecimal(releaseRequest.getCommissionObject().getAmount()),
                    releaseRequest.getCommissionObject().getCurrency(),
                    RequestContext.getClientIp()
                )
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Release completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Release failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Release unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void inquiry(InquiryCollateralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Inquiry called with uniqueIdentifier: {}", request.getUniqueIdentifier());
            
            CollateralTrackResponse trackResponse = collateralOperationService.inquiry(
                RequestContext.getChannelEntity(),
                request.getUniqueIdentifier(),
                RequestContext.getClientIp()
            );
            
            // Convert to GRPC response
            CollateralTrackResponseGrpc.Builder trackResponseBuilder = CollateralTrackResponseGrpc.newBuilder()
                    .setCollateralCreateTrackObject(CollateralCreateTrackObject.newBuilder()
                            .setId(Long.parseLong(trackResponse.getCollateralCreateTrackObject().getId()))
                            .setCollateralCode(trackResponse.getCollateralCreateTrackObject().getCollateralCode())
                            .setAdditionalData(trackResponse.getCollateralCreateTrackObject().getAdditionalData())
                            .setCurrency(trackResponse.getCollateralCreateTrackObject().getCurrency())
                            .setChannelName(trackResponse.getCollateralCreateTrackObject().getChannelName())
                            .setCommission(trackResponse.getCollateralCreateTrackObject().getCommission())
                            .setCreateTime(trackResponse.getCollateralCreateTrackObject().getCreateTime())
                            .setCreateTimeTimestamp(trackResponse.getCollateralCreateTrackObject().getCreateTimeTimestamp())
                            .setCurrency(trackResponse.getCollateralCreateTrackObject().getCurrency())
                            .setNationalCode(trackResponse.getCollateralCreateTrackObject().getNationalCode())
                            .setFinalQuantityBlock(trackResponse.getCollateralCreateTrackObject().getFinalQuantityBlock())
                            .setQuantity(trackResponse.getCollateralCreateTrackObject().getQuantity())
                            .setResult(Integer.parseInt(trackResponse.getCollateralCreateTrackObject().getResult()))
                            .setUniqueIdentifier(trackResponse.getCollateralCreateTrackObject().getUniqueIdentifier())
                    )

                    .setCollateralIncreaseTrackObject(CollateralIncreaseTrackObject.newBuilder()
                            .setChannelName(trackResponse.getCollateralCreateTrackObject().getChannelName())
                            .setDescription(trackResponse.getCollateralCreateTrackObject().getDescription())
                            .setCommission(trackResponse.getCollateralCreateTrackObject().getCommission())
                            .setCreateTime(trackResponse.getCollateralCreateTrackObject().getCreateTime())
                            .setCreateTimeTimestamp(trackResponse.getCollateralCreateTrackObject().getCreateTimeTimestamp())
                            .setQuantity(trackResponse.getCollateralCreateTrackObject().getQuantity())
                            .setResult(Integer.parseInt(trackResponse.getCollateralCreateTrackObject().getResult()))
                            .setId(Long.parseLong(trackResponse.getCollateralCreateTrackObject().getId()))
                    )
                    .setCollateralReleaseTrackObject(CollateralReleaseTrackObject.newBuilder()
                            .setChannelName(trackResponse.getCollateralCreateTrackObject().getChannelName())
                            .setCommission(trackResponse.getCollateralCreateTrackObject().getCommission())
                            .setQuantity(trackResponse.getCollateralCreateTrackObject().getQuantity())
                            .setDescription(trackResponse.getCollateralCreateTrackObject().getDescription())
                            .setCreateTime(trackResponse.getCollateralCreateTrackObject().getCreateTime())
                            .setCreateTimeTimestamp(trackResponse.getCollateralCreateTrackObject().getCreateTimeTimestamp())
                            .setResult(Integer.parseInt(trackResponse.getCollateralCreateTrackObject().getResult()))
                    );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setCollateralTrackResponse(trackResponseBuilder.build())
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
    public void increaseGenerateUuid(UniqueIdentifierCollateralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: IncreaseGenerateUuid called with nationalCode: {}, quantity: {}, currency: {}", 
                request.getNationalCode(), request.getQuantity(), request.getCurrency());
            
            UniqueIdentifierCollateralRequestJson generateUuidRequest = new UniqueIdentifierCollateralRequestJson();
            generateUuidRequest.setNationalCode(request.getNationalCode());
            generateUuidRequest.setQuantity(request.getQuantity());
            generateUuidRequest.setCurrency(request.getCurrency());
            generateUuidRequest.setWalletAccountNumber(request.getWalletAccountNumber());
            
            UuidResponse uuid = collateralOperationService.generateIncreaseUniqueIdentifier(
                RequestContext.getChannelEntity(),
                generateUuidRequest.getNationalCode(),
                generateUuidRequest.getQuantity(),
                generateUuidRequest.getCurrency(),
                generateUuidRequest.getWalletAccountNumber()
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setUuidResponse(UuidResponseGrpc.newBuilder()
                    .setUuid(uuid.getUniqueIdentifier())
                    .build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: IncreaseGenerateUuid completed successfully with uuid: {}", uuid.getUniqueIdentifier());
            
        } catch (InternalServiceException e) {
            log.error("GRPC: IncreaseGenerateUuid failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: IncreaseGenerateUuid unexpected error: {}", e.getMessage(), e);
            handleUnexpectedError(responseObserver, e);
        }
    }

    @Override
    public void increase(IncreaseCollateralRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            log.info("GRPC: Increase called with collateralCode: {}, nationalCode: {}", 
                request.getCollateralCode(), request.getNationalCode());
            
            IncreaseCollateralRequestJson increaseRequest = new IncreaseCollateralRequestJson();
            increaseRequest.setCollateralCode(request.getCollateralCode());
            increaseRequest.setQuantity(request.getQuantity());
            increaseRequest.setNationalCode(request.getNationalCode());
            increaseRequest.setAdditionalData(request.getAdditionalData());
            increaseRequest.setSign(request.getSign());
            increaseRequest.setUniqueIdentifier(request.getUniqueIdentifier());
            
            // Convert CommissionObjectGrpc to CommissionObject
            CommissionObject commissionObject = new CommissionObject();
            commissionObject.setAmount(request.getCommissionObject().getAmount());
            commissionObject.setCurrency(request.getCommissionObject().getCurrency());
            increaseRequest.setCommissionObject(commissionObject);
            
            collateralOperationService.increase(
                new IncreaseCollateralObjectDTO(
                    RequestContext.getChannelEntity(),
                    increaseRequest.getCollateralCode(),
                    new BigDecimal(increaseRequest.getQuantity()),
                    increaseRequest.getNationalCode(),
                    increaseRequest.getAdditionalData(),
                    new BigDecimal(increaseRequest.getCommissionObject().getAmount()),
                    increaseRequest.getCommissionObject().getCurrency(),
                    RequestContext.getClientIp(),
                    increaseRequest.getUniqueIdentifier()
                )
            );
            
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                .setSuccess(true)
                .setEmpty(Empty.newBuilder().build())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("GRPC: Increase completed successfully");
            
        } catch (InternalServiceException e) {
            log.error("GRPC: Increase failed: {}", e.getMessage(), e);
            handleError(responseObserver, e);
        } catch (Exception e) {
            log.error("GRPC: Increase unexpected error: {}", e.getMessage(), e);
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
