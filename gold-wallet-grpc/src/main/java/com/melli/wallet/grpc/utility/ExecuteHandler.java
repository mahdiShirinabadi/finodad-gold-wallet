package com.melli.wallet.grpc.utility;

import com.melli.wallet.grpc.BaseResponseGrpc;
import com.melli.wallet.grpc.exception.GrpcErrorHandler;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: ExecuteHandler
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@Log4j2
@Service

public class ExecuteHandler {

    private static GrpcErrorHandler exceptionHandler;

    public ExecuteHandler() {
    }

    public ExecuteHandler(GrpcErrorHandler exceptionHandler) {
        ExecuteHandler.exceptionHandler = exceptionHandler;
    }

    public static <T> void executeWithErrorHandling(StreamObserver<T> responseObserver, String methodName, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            // Cast is safe here because we know this service always uses BaseResponseGrpc
            exceptionHandler.handleException(ex, (StreamObserver<BaseResponseGrpc>) responseObserver, methodName);
        }
    }
}
