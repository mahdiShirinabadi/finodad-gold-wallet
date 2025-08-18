package com.melli.wallet.grpc.interceptor;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.persistence.ChannelRoleRepository;
import com.melli.wallet.grpc.config.RequestContext;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthorizationInterceptor implements ServerInterceptor {

    private final ChannelRoleRepository channelRoleRepository;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ChannelEntity channelEntity = RequestContext.getChannelEntity();
        if (channelEntity == null) {
            log.warn("No channel entity found in request context");
            call.close(Status.PERMISSION_DENIED.withDescription("No channel entity found"), headers);
            return new ServerCall.Listener<>() {};
        }

        String methodName = call.getMethodDescriptor().getFullMethodName();
        String requiredResource = getRequiredResource(methodName);

        if (requiredResource == null) {
            log.warn("No required resource found for method: {}", methodName);
            call.close(Status.PERMISSION_DENIED.withDescription("No required resource defined"), headers);
            return new ServerCall.Listener<>() {};
        }

        if (!hasPermission(channelEntity, requiredResource)) {
            log.warn("Channel {} does not have permission for resource: {}", 
                    channelEntity.getUsername(), requiredResource);
            call.close(Status.PERMISSION_DENIED.withDescription("Insufficient permissions"), headers);
            return new ServerCall.Listener<>() {};
        }

        log.info("Authorization successful for channel: {} on method: {}", 
                channelEntity.getUsername(), methodName);

        return next.startCall(call, headers);
    }

    private String getRequiredResource(String methodName) {
        // Map gRPC method names to required resources
        return switch (methodName) {
            case "wallet.AuthService/Login", "wallet.AuthService/RefreshToken", "wallet.AuthService/Logout" ->
                    null; // No authorization required for auth methods

            case "wallet.MerchantService/GetMerchant" -> "MERCHANT_LIST";
            case "wallet.PurchaseService/GenerateBuyUuid", "wallet.PurchaseService/GenerateSellUuid",
                 "wallet.PurchaseService/Inquiry" -> "GENERATE_PURCHASE_UNIQUE_IDENTIFIER";
            case "wallet.PurchaseService/Buy" -> "BUY";
            case "wallet.PurchaseService/BuyDirect" -> "BUY_DIRECT";
            case "wallet.PurchaseService/Sell" -> "SELL";
            case "wallet.WalletService/CreateWallet" -> "WALLET_CREATE";
            case "wallet.WalletService/DeactivateWallet" -> "WALLET_DEACTIVATE";
            case "wallet.WalletService/DeleteWallet" -> "WALLET_DELETE";
            case "wallet.WalletService/GetWallet" -> "WALLET_INFO";
            case "wallet.WalletService/ActivateWallet" -> "WALLET_ACTIVE";
            case "wallet.LimitationService/GetLimitationValue", "wallet.LimitationService/GetLimitationList" ->
                    "LIMITATION_LIST";
            default -> {
                log.warn("Unknown method: {}", methodName);
                yield null;
            }
        };
    }

    private boolean hasPermission(ChannelEntity channelEntity, String requiredResource) {
        try {
            // Get channel roles
            List<ChannelRoleEntity> channelRoles = channelRoleRepository.findByChannelId(channelEntity.getId());
            
            if (channelRoles.isEmpty()) {
                log.warn("No roles found for channel: {}", channelEntity.getUsername());
                return false;
            }

            // Get all resources from all roles
            Set<String> availableResources = channelRoles.stream()
                    .map(ChannelRoleEntity::getRoleEntity)
                    .map(RoleEntity::getResources)
                    .flatMap(Set::stream)
                    .map(ResourceEntity::getName)
                    .collect(Collectors.toSet());

            boolean hasPermission = availableResources.contains(requiredResource);
            
            if (!hasPermission) {
                log.warn("Channel {} has resources: {} but needs: {}", 
                        channelEntity.getUsername(), availableResources, requiredResource);
            }

            return hasPermission;
            
        } catch (Exception e) {
            log.error("Error checking permissions for channel: {}", channelEntity.getUsername(), e);
            return false;
        }
    }
}
