package com.melli.wallet.grpc.interceptor;


import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.melli.wallet.grpc.*;
import io.grpc.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class Name: AuthorizationInterceptor
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */

@Component
@Log4j2
public class CustomAuthorizationInterceptor implements ServerInterceptor {

    // Map service names to their ServiceDescriptor providers
    private static final Map<String, Descriptors.ServiceDescriptor> SERVICE_DESCRIPTOR_MAP = new HashMap<>();

    static {
        List<Descriptors.FileDescriptor> fileDescriptors = Arrays.asList(
                AuthProto.getDescriptor(),
                MerchantProto.getDescriptor(),
                WalletProto.getDescriptor(),
                PurchaseProto.getDescriptor(),
                LimitationProto.getDescriptor()
        );

        for (Descriptors.FileDescriptor fileDescriptor : fileDescriptors) {
            for (Descriptors.ServiceDescriptor service : fileDescriptor.getServices()) {
                SERVICE_DESCRIPTOR_MAP.put(service.getFullName(), service);
            }
        }
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("CustomAuthorizationInterceptor invoked for method: {}", methodName);

        String requiredResource = getRequiredResource(call.getMethodDescriptor());
        if (!requiredResource.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getAuthorities().stream()
                    .noneMatch(auth -> auth.getAuthority().equals(requiredResource))) {
                log.error("Unauthorized access to {}: Missing {} authority", methodName, requiredResource);
                call.close(Status.PERMISSION_DENIED.withDescription("Missing " + requiredResource + " authority"),
                        new Metadata());
                return new ServerCall.Listener<>() {};
            }
            log.info("Authorized access to {} for resource: {}", methodName, requiredResource);
        } else {
            log.debug("No required resource specified for method: {}", methodName);
        }

        return next.startCall(call, headers);
    }

    private String getRequiredResource(MethodDescriptor<?, ?> methodDescriptor) {
        try {
            String fullMethodName = methodDescriptor.getFullMethodName(); // e.g., wallet.PurchaseService/Buy
            String serviceName = fullMethodName.substring(0, fullMethodName.lastIndexOf('/')); // e.g., wallet.PurchaseService
            String methodSimpleName = methodDescriptor.getBareMethodName(); // e.g., Buy
            if (methodSimpleName == null) {
                log.warn("Method simple name is null for: {}", fullMethodName);
                return "";
            }

            Descriptors.ServiceDescriptor serviceDescriptor = findServiceDescriptor(serviceName);
            if (serviceDescriptor != null) {
                Descriptors.MethodDescriptor protoMethodDescriptor = serviceDescriptor.findMethodByName(methodSimpleName);
                if (protoMethodDescriptor != null) {
                    DescriptorProtos.MethodOptions options = protoMethodDescriptor.getOptions();
                    if (options.hasExtension(AuthOptions.requiredResource)) {
                        String resource = options.getExtension(AuthOptions.requiredResource);
                        log.debug("Found required resource: {} for method: {}", resource, fullMethodName);
                        return resource;
                    } else {
                        log.debug("No required_resource extension for method: {}", fullMethodName);
                    }
                } else {
                    log.warn("Method descriptor not found for: {}", methodSimpleName);
                }
            } else {
                log.warn("Service descriptor not found for: {}", serviceName);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve required resource for method {}: {}",
                    methodDescriptor.getFullMethodName(), e.getMessage(), e);
        }
        return "";
    }

    private Descriptors.ServiceDescriptor findServiceDescriptor(String serviceName) {
        Descriptors.ServiceDescriptor grpcDescriptor = SERVICE_DESCRIPTOR_MAP.get(serviceName);
        if (grpcDescriptor == null) {
            log.warn("No ServiceDescriptor found for service: {}", serviceName);
            return null;
        }

        return grpcDescriptor.getFile().getServices().stream()
                .filter(s -> s.getFullName().equals(serviceName))
                .findFirst()
                .orElse(null);
    }
}
