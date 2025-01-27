package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.persistence.ChannelRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ChannelService;
import com.melli.hub.service.StatusService;
import com.melli.hub.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.melli.hub.utils.Constant.CHANNEL_NAME_CACHE;


@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = CHANNEL_NAME_CACHE)
public class ChannelServiceImplementation implements ChannelService {


    private final ChannelRepository channelRepository;

    @Override
    public void init() {
        log.info("Start init channel .....");
    }


    @Cacheable(key = "{#channelName}", unless = "#result == null")
    public ChannelEntity getChannel(String channelName) {
        log.info("find channel with name ===> {}", channelName);
        return channelRepository.findByUsername(channelName);
    }

    @Override
    public ChannelEntity findById(Long channelId) throws InternalServiceException {
        return channelRepository.findById(channelId).orElseThrow(()->{
            log.error("channel with id ({}) not found", channelId);
            return new InternalServiceException("channel not found", StatusService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    public ChannelEntity findByUsername(String username) {
        return channelRepository.findByUsername(username);
    }

    @Override
    public void save(ChannelEntity channelEntity){
        channelRepository.save(channelEntity);
    }

    @Override
    public ChannelEntity saveChannel(ChannelEntity channelEntity) throws InternalServiceException {
        log.info("start save channel in channel service impl with parameters : userName ={} ,trust ={}, sign ={},name ={},publicKey ={},ip ={},status ={}, account ={},balanceLimitation ={},checkShahkar ={},wageIban ={},wageAccount ={},",
                channelEntity.getUsername(), channelEntity.getTrust(), channelEntity.getSign(), channelEntity.getLastName(), channelEntity.getPublicKey(), channelEntity.getIp(), channelEntity.getStatus(), channelEntity.getAccount(), channelEntity.getBalanceLimitation(), channelEntity.getCheckShahkar(), channelEntity.getWageIban(), channelEntity.getWageAccount());
        if (getChannel(channelEntity.getUsername()) != null) {
            log.error("in create channel service ,channel by channel name = {} exist", channelEntity.getUsername());
            throw new InternalServiceException("channel with username is duplicate", StatusService.DUPLICATE_CHANNEL_NAME, HttpStatus.OK);
        }
        return channelRepository.save(channelEntity);
    }


    @CacheEvict
    @Override
    public void clearCache(String channelName) {
        log.info("start clear channel, channelName ==> {}", channelName);
    }

    @CacheEvict(allEntries = true)
    @Override
    public void clearCacheAll() {
        log.info("start clear all channel");
    }


    @Override
    public ChannelEntity changePasswordChannel(String channelId, String password, PasswordEncoder bcryptEncoder) throws InternalServiceException {
        log.info("change password for channel with channelName ({}) ", channelId);
        ChannelEntity channelEntity = channelRepository.findById(Long.parseLong(channelId)).orElseThrow(() -> {
            log.error("channel with id ({}) not found", channelId);
            return new InternalServiceException("channel not found", StatusService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        });

        if (!StringUtils.hasText(password)) {
            log.error("in change Password Channel service ,password not valid by channel name = {} ", channelId);
            throw new InternalServiceException("channel not found", StatusService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        }
        channelEntity.setPassword(bcryptEncoder.encode(password));
        return channelRepository.save(channelEntity);
    }
}

