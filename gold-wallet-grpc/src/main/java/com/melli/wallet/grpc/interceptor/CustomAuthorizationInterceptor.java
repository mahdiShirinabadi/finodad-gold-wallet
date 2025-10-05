package com.melli.wallet.grpc.interceptor;


import io.grpc.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Class Name: AuthorizationInterceptor
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */

@Component
@Log4j2
public class CustomAuthorizationInterceptor implements ServerInterceptor {

    static {
        // Initialize interceptor
        log.info("CustomAuthorizationInterceptor initialized");
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

            // For now, we'll use a simple mapping approach since AuthOptions is not available
            // This can be enhanced later with proper authorization annotations
            String requiredResource = getResourceFromMethodName(serviceName, methodSimpleName);
            
            if (!requiredResource.isEmpty()) {
                log.debug("Found required resource: {} for method: {}", requiredResource, fullMethodName);
                return requiredResource;
            } else {
                log.debug("No required resource specified for method: {}", fullMethodName);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve required resource for method {}: {}",
                    methodDescriptor.getFullMethodName(), e.getMessage(), e);
        }
        return "";
    }
    
    private String getResourceFromMethodName(String serviceName, String methodName) {
        // Simple mapping for common authorization patterns
        // This can be enhanced with a more sophisticated approach
        if (serviceName.contains("AuthenticationService")) {
            return ""; // Authentication methods don't require additional authorization
        } else if (serviceName.contains("PurchaseService")) {
            if (methodName.contains("Buy")) {
                return "BUY_AUTH";
            } else if (methodName.contains("Sell")) {
                return "SELL_AUTH";
            }
        } else if (serviceName.contains("CashInService")) {
            return "CASH_IN_AUTH";
        } else if (serviceName.contains("CashOutService")) {
            return "CASH_OUT_AUTH";
        } else if (serviceName.contains("CollateralService")) {
            return "COLLATERAL_AUTH";
        } else if (serviceName.contains("P2pService")) {
            return "P2P_AUTH";
        } else if (serviceName.contains("GiftCardService")) {
            return "GIFT_CARD_AUTH";
        } else if (serviceName.contains("MerchantService")) {
            return "MERCHANT_LIST_AUTH";
        } else if (serviceName.contains("LimitationService")) {
            return "LIMITATION_LIST_AUTH";
        } else if (serviceName.contains("TransactionService")) {
            return "STATEMENT_AUTH";
        } else if (serviceName.contains("PhysicalCashOutService")) {
            return "PHYSICAL_CASH_OUT_AUTH";
        } else if (serviceName.contains("Panel")) {
            return "LIMITATION_MANAGE_AUTH";
        }
        
        return ""; // Default: no specific authorization required
    }

}
