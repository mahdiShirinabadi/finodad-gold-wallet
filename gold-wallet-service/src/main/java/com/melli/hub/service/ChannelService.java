package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.exception.InternalServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by shirinabadi on 03/11/2016.
 */
public interface ChannelService {

	int DEACTIVATE = 0;
	int ACTIVE = 1;

	int TRUE = 1;
	int FALSE = 0;

	void init();

	ChannelEntity getChannel(String channelName);

	ChannelEntity findById(int channelId);

	@Transactional
	void save(ChannelEntity channelEntity) throws InternalServiceException;

	@Transactional
	ChannelEntity saveChannel(ChannelEntity channel) throws InternalServiceException;

	void clearCache(String channelName);

	void clearCacheAll();

	ChannelEntity changePasswordChannel(String channelName,String password,PasswordEncoder bcryptEncoder)throws InternalServiceException;
}
