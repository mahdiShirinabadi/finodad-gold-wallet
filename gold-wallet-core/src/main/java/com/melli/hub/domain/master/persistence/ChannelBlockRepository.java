package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelBlockEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelBlockRepository extends CrudRepository<ChannelBlockEntity, Long> {
	ChannelBlockEntity findByChannelEntityId(long channelId);
}
