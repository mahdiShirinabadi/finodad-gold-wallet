package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelBlockEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileBlockRepository extends CrudRepository<ChannelBlockEntity, Long> {
	ChannelBlockEntity findByProfileEntityId(long channelId);
}
