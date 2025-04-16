package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ChannelBlockEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelBlockRepository extends CrudRepository<ChannelBlockEntity, Long> {
	ChannelBlockEntity findByChannelEntityId(long channelId);
}
