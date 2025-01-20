package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelBlockEntity;
import com.melli.hub.domain.master.entity.ChannelEntity;

public interface ChannelBlockService {

	ChannelBlockEntity findByProfile(ChannelEntity channelEntity);

	void save(ChannelBlockEntity channelBlockEntity);

	void clearCache();

}
