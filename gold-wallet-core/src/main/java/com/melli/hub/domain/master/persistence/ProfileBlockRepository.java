package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ProfileBlockEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileBlockRepository extends CrudRepository<ProfileBlockEntity, Long> {
	ProfileBlockEntity findByProfileEntityId(long channelId);
}
