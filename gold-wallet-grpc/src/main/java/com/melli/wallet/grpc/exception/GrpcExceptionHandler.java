package com.melli.wallet.grpc.exception;

import com.melli.wallet.domain.response.base.ErrorDetail;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.grpc.BaseResponseGrpc;
import com.melli.wallet.grpc.ErrorDetailGrpc;
import com.melli.wallet.service.operation.AlertService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

/**
 * Class Name: GrpcExceptionHandler
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class GrpcExceptionHandler {

    private final StatusRepositoryService statusRepositoryService;
    private final AlertService alertService;

    public <T> void handleException(Exception ex, StreamObserver<BaseResponseGrpc> responseObserver, String methodName) {
        log.error("Exception in gRPC method {}: {}", methodName, ex.getMessage(), ex);

        ErrorDetail errorDetail = new ErrorDetail();

        try {
            if (ex instanceof AuthenticationException || ex instanceof BadCredentialsException) {
                errorDetail.setCode(StatusRepositoryService.TOKEN_NOT_VALID);
                errorDetail.setMessage(statusRepositoryService.findByCode(String.valueOf(StatusRepositoryService.TOKEN_NOT_VALID)).getPersianDescription());
                alertService.send("AuthenticationException in " + methodName + ": " + ex.getMessage(), "");
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription("Authentication failed").asException());
            } else if (ex instanceof AccessDeniedException) {
                errorDetail.setCode(StatusRepositoryService.USER_NOT_PERMISSION);
                errorDetail.setMessage(statusRepositoryService.findByCode(String.valueOf(StatusRepositoryService.USER_NOT_PERMISSION)).getPersianDescription());
                alertService.send("AccessDeniedException in " + methodName + ": " + ex.getMessage(), "");
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Access denied").asException());
            } else if (ex instanceof InternalServiceException ise) {
                errorDetail.setCode(ise.getStatus());
                String message = statusRepositoryService.findByCode(String.valueOf(ise.getStatus())).getPersianDescription();
                errorDetail.setMessage(message);
                alertService.send("InternalServiceException in " + methodName + ": " + ex.getMessage(), "");
                responseObserver.onNext(BaseResponseGrpc.newBuilder()
                        .setSuccess(false)
                        .setDoTime("")
                        .setErrorDetail(ErrorDetailGrpc.newBuilder().setCode(String.valueOf(errorDetail.getCode()))
                                .setMessage(errorDetail.getMessage()).build()).build());
                responseObserver.onCompleted();
            } else if (ex instanceof ConstraintViolationException ce) {
                ConstraintViolation<?> violation = ce.getConstraintViolations().iterator().next();
                errorDetail.setCode(StatusRepositoryService.INPUT_PARAMETER_NOT_VALID);
                errorDetail.setMessage(violation.getMessage());
                alertService.send("ConstraintViolationException in " + methodName + ": " + errorDetail.getMessage(), "");
                responseObserver.onNext(BaseResponseGrpc.newBuilder()
                        .setSuccess(false)
                        .setDoTime("")
                        .setErrorDetail(ErrorDetailGrpc.newBuilder().setCode(String.valueOf(errorDetail.getCode()))
                                .setMessage(errorDetail.getMessage()).build()).build());
                responseObserver.onCompleted();
            } else {
                log.error("general error", ex);
                errorDetail.setCode(StatusRepositoryService.GENERAL_ERROR);
                errorDetail.setMessage(statusRepositoryService.findByCode(String.valueOf(StatusRepositoryService.GENERAL_ERROR)).getPersianDescription());
                alertService.send("Exception in " + methodName + ": " + ex.getMessage(), "");
                responseObserver.onNext(BaseResponseGrpc.newBuilder()
                        .setSuccess(false)
                        .setDoTime("")
                        .setErrorDetail(ErrorDetailGrpc.newBuilder().setCode(String.valueOf(errorDetail.getCode()))
                                .setMessage(errorDetail.getMessage()).build()).build());
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            log.error("error in try/catch", e);
            errorDetail.setCode(StatusRepositoryService.GENERAL_ERROR);
            errorDetail.setMessage("Error processing exception");
            responseObserver.onNext(BaseResponseGrpc.newBuilder()
                    .setSuccess(false)
                    .setDoTime("")
                    .setErrorDetail(ErrorDetailGrpc.newBuilder().setCode(String.valueOf(errorDetail.getCode()))
                            .setMessage(errorDetail.getMessage()).build()).build());
            responseObserver.onCompleted();
        }
    }
}
