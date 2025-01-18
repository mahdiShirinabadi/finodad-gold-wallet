package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, Long> {

    ChannelEntity findByUsername(String channelName);
    ChannelEntity findById(int id);
}
