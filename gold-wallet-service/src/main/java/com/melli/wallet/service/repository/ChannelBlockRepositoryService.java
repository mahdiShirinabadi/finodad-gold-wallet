package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelBlockEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;

public interface ChannelBlockRepositoryService {

	ChannelBlockEntity findByProfile(ChannelEntity channelEntity);

	void save(ChannelBlockEntity channelBlockEntity);

	void clearCache();

}
