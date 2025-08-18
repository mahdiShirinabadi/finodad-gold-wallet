package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.request.panel.ResourceCreateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceUpdateRequestJson;
import com.melli.wallet.domain.response.panel.ResourceDetailResponse;
import com.melli.wallet.domain.response.panel.ResourceListResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResourceRepositoryService {
    String LOGOUT = "LOGOUT";
    String WALLET_CREATE = "WALLET_CREATE";
    String WALLET_DEACTIVATE = "WALLET_DEACTIVATE";
    String WALLET_DELETE = "WALLET_DELETE";
    String WALLET_ACTIVE = "WALLET_ACTIVE";
    String WALLET_INFO = "WALLET_INFO";
    String GENERATE_CASH_IN_UNIQUE_IDENTIFIER = "GENERATE_CASH_IN_UNIQUE_IDENTIFIER";
    String GENERATE_PURCHASE_UNIQUE_IDENTIFIER = "GENERATE_PURCHASE_UNIQUE_IDENTIFIER";
    String MERCHANT_LIST = "MERCHANT_LIST";
    String CASH_IN = "CASH_IN";
    String CASH_OUT = "CASH_OUT";
    String PHYSICAL_CASH_OUT = "PHYSICAL_CASH_OUT";
    String P2P = "P2P";
    String BUY = "BUY";
    String BUY_DIRECT = "BUY_DIRECT";
    String SELL = "SELL";
    String SETTING_LIST = "SETTING_LIST";
    String LIMITATION_LIST = "LIMITATION_LIST";
    String LIMITATION_MANAGE = "LIMITATION_MANAGE";
    String MERCHANT_BALANCE = "MERCHANT_BALANCE";
    String MERCHANT_INCREASE_BALANCE = "MERCHANT_INCREASE_BALANCE";
    String MERCHANT_DECREASE_BALANCE = "MERCHANT_DECREASE_BALANCE";
    String MERCHANT_MANAGE = "MERCHANT_MANAGE";
    String STATEMENT = "STATEMENT";
    String PANEL_CHANNEL_LIST = "PANEL_CHANNEL_LIST";
    

    // Panel Management Resources (Simplified)
    String ROLE_MANAGE = "ROLE_MANAGE";
    String RESOURCE_MANAGE = "RESOURCE_MANAGE";
    String CHANNEL_MANAGE = "CHANNEL_MANAGE";

    ResourceEntity getRequestType(String name);

    List<ResourceEntity> findAllByIds(List<Long> ids);

    /**
     * Create a new resource
     */
    ResourceDetailResponse createResource(ResourceCreateRequestJson requestJson, String createdBy) throws InternalServiceException;

    /**
     * Update an existing resource
     */
    ResourceDetailResponse updateResource(Long resourceId, ResourceUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException;

    /**
     * Delete a resource
     */
    void deleteResource(Long resourceId) throws InternalServiceException;

    /**
     * Get resource by ID
     */
    ResourceDetailResponse getResourceById(Long resourceId) throws InternalServiceException;

    /**
     * Get paginated list of resources
     */
    Page<ResourceListResponse> listResources(Pageable pageable) throws InternalServiceException;

}
