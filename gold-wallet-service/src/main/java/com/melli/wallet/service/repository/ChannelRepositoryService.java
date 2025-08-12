package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by shirinabadi on 03/11/2016.
 */
public interface ChannelRepositoryService {

	int DEACTIVATE = 0;
	int ACTIVE = 1;

	int TRUE = 1;
	int FALSE = 0;

	void init();

	ChannelEntity getChannel(String channelName);

	ChannelEntity findById(Long channelId) throws InternalServiceException;
	ChannelEntity findByUsername(String username);

	@Transactional
	void save(ChannelEntity channelEntity);

	@Transactional
	ChannelEntity saveChannel(ChannelEntity channel) throws InternalServiceException;

	void clearCache(String channelName);

	void clearCacheAll();

	ChannelEntity changePasswordChannel(String channelName, String password, PasswordEncoder bcryptEncoder)throws InternalServiceException;
}
