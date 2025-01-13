package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelBlockEntity;

public interface ProfileBlockService {

	ChannelBlockEntity findByProfile(ProfileEntity profileEntity);

	void save(ChannelBlockEntity channelBlockEntity);

	void clearCache();

}
