package com.melli.wallet.grpc.exception;

import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.StatusRepositoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class GrpcErrorHandler {

    public void handleException(InternalServiceException e, StreamObserver<?> responseObserver, String context) {
        log.error("InternalServiceException in {}: {}", context, e.getMessage());
        
        Status status = mapExceptionToStatus(e);
        responseObserver.onError(status.withDescription(e.getMessage()).asRuntimeException());
    }

    public void handleException(Exception e, StreamObserver<?> responseObserver, String context) {
        log.error("Unexpected exception in {}: {}", context, e.getMessage(), e);
        
        Status status = Status.INTERNAL;
        responseObserver.onError(status.withDescription("Internal server error").asRuntimeException());
    }

    private Status mapExceptionToStatus(InternalServiceException e) {
        int errorCode = e.getStatus();
        
        return switch (errorCode) {
            case StatusRepositoryService.CHANNEL_NOT_FOUND -> Status.NOT_FOUND;
            case StatusRepositoryService.CHANNEL_IS_DISABLE -> Status.PERMISSION_DENIED;
            case StatusRepositoryService.INVALID_USERNAME_PASSWORD -> Status.UNAUTHENTICATED;
            case StatusRepositoryService.TOKEN_NOT_VALID -> Status.UNAUTHENTICATED;
            case StatusRepositoryService.ROLE_NOT_FOUND -> Status.NOT_FOUND;
            case StatusRepositoryService.RESOURCE_NOT_FOUND -> Status.NOT_FOUND;
            case StatusRepositoryService.ROLE_ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case StatusRepositoryService.RESOURCE_ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case StatusRepositoryService.ROLE_IN_USE -> Status.FAILED_PRECONDITION;
            case StatusRepositoryService.RESOURCE_IN_USE -> Status.FAILED_PRECONDITION;
            case StatusRepositoryService.ROLE_ALREADY_ASSIGNED -> Status.ALREADY_EXISTS;
            case StatusRepositoryService.ROLE_NOT_ASSIGNED -> Status.NOT_FOUND;
            case StatusRepositoryService.MERCHANT_IS_DISABLE -> Status.PERMISSION_DENIED;
            case StatusRepositoryService.INTERNAL_ERROR -> Status.INTERNAL;
            default -> Status.INTERNAL;
        };
    }
}
