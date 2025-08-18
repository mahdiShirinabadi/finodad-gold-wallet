package com.melli.wallet.service.operation;

import com.melli.wallet.domain.response.panel.ChannelListResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChannelRoleOperationService {
    
    /**
     * Assign role to channel
     */
    @Transactional
    void assignRoleToChannel(Long roleId, Long channelId, String createdBy) throws InternalServiceException;
    

    /**
     * Get channel list
     */
    List<ChannelListResponse> getChannelList() throws InternalServiceException;
}
