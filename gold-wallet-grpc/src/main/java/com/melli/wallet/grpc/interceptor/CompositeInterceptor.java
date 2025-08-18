package com.melli.wallet.grpc.interceptor;

import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class CompositeInterceptor implements ServerInterceptor {

    private final JwtAuthenticationInterceptor authenticationInterceptor;
    private final AuthorizationInterceptor authorizationInterceptor;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // First, handle authentication
        ServerCall.Listener<ReqT> authListener = authenticationInterceptor.interceptCall(call, headers, next);
        
        // Then, handle authorization
        return authorizationInterceptor.interceptCall(call, headers, next);
    }
}
