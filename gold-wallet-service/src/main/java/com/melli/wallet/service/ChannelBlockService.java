package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelBlockEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;

public interface ChannelBlockService {

	ChannelBlockEntity findByProfile(ChannelEntity channelEntity);

	void save(ChannelBlockEntity channelBlockEntity);

	void clearCache();

}
