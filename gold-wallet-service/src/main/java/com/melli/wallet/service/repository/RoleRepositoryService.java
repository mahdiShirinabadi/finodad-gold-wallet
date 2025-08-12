package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: RoleService
 * Author: Mahdi Shirinabadi
 * Date: 1/5/2025
 */
public interface RoleRepositoryService {

    String WEB_PROFILE = "WEB_PROFILE";

    void addChannelToRole(ChannelEntity channelEntity, String roleName) throws InternalServiceException;
    void removeChannelFromRole(ChannelEntity channelEntity, String roleName) throws InternalServiceException;
}
