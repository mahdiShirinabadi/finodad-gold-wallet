package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.ChannelBlockEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.persistence.ChannelBlockRepository;
import com.melli.wallet.service.repository.ChannelBlockRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: ProfileBlockServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@RequiredArgsConstructor
@Log4j2
@Service
public class ChannelBlockRepositoryServiceImplementation implements ChannelBlockRepositoryService {

    private final ChannelBlockRepository channelBlockRepository;

    @Override
    public ChannelBlockEntity findByProfile(ChannelEntity channelEntity)
    {
        return channelBlockRepository.findByChannelEntityId(channelEntity.getId());
    }

    @Override
    public void save(ChannelBlockEntity channelBlockEntity) {
        channelBlockRepository.save(channelBlockEntity);
    }

    @Override
    public void clearCache() {

    }
}
