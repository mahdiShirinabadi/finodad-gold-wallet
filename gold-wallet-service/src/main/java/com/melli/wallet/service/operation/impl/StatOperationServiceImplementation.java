package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.StatBuyEntity;
import com.melli.wallet.domain.master.entity.StatSellEntity;
import com.melli.wallet.domain.master.entity.StatWalletEntity;
import com.melli.wallet.domain.master.entity.StatPerson2PersonEntity;
import com.melli.wallet.domain.master.entity.StatPhysicalCashOutEntity;
import com.melli.wallet.service.repository.StatBuyRepositoryService;
import com.melli.wallet.service.repository.StatSellRepositoryService;
import com.melli.wallet.service.repository.StatWalletRepositoryService;
import com.melli.wallet.service.repository.StatPerson2PersonRepositoryService;
import com.melli.wallet.service.repository.StatPhysicalCashOutRepositoryService;
import com.melli.wallet.domain.response.stat.StatBuyListResponse;
import com.melli.wallet.domain.response.stat.StatSellListResponse;
import com.melli.wallet.domain.response.stat.StatWalletListResponse;
import com.melli.wallet.domain.response.stat.StatPerson2PersonListResponse;
import com.melli.wallet.domain.response.stat.StatPhysicalCashOutListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.StatOperationService;
import com.melli.wallet.utils.StatResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class Name: StatOperationServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of statistics operations
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StatOperationServiceImplementation implements StatOperationService {

    private final StatBuyRepositoryService statBuyRepositoryService;
    private final StatSellRepositoryService statSellRepositoryService;
    private final StatWalletRepositoryService statWalletRepositoryService;
    private final StatPerson2PersonRepositoryService statPerson2PersonRepositoryService;
    private final StatPhysicalCashOutRepositoryService statPhysicalCashOutRepositoryService;
    private final StatResponseHelper statResponseHelper;

    @Override
    public StatBuyListResponse getBuyStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start get buy statistics for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        Pageable pageable = getPageableConfig(searchCriteria);
        Page<StatBuyEntity> statPage = statBuyRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        log.info("finish get buy statistics, found {} records", statPage.stream().toList().size());

        return statResponseHelper.fillStatBuyListResponse(statPage);
    }

    @Override
    public StatSellListResponse getSellStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start get sell statistics for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        Pageable pageable = getPageableConfig(searchCriteria);
        Page<StatSellEntity> statPage = statSellRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        log.info("finish get sell statistics, found {} records", statPage.stream().toList().size());

        return statResponseHelper.fillStatSellListResponse(statPage);
    }

    @Override
    public StatWalletListResponse getWalletStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start get wallet statistics for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        Pageable pageable = getPageableConfig(searchCriteria);
        Page<StatWalletEntity> statPage = statWalletRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        log.info("finish get wallet statistics, found {} records", statPage.stream().toList().size());

        return statResponseHelper.fillStatWalletListResponse(statPage);
    }

    @Override
    public StatPerson2PersonListResponse getPerson2PersonStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start get person2person statistics for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        Pageable pageable = getPageableConfig(searchCriteria);
        Page<StatPerson2PersonEntity> statPage = statPerson2PersonRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        log.info("finish get person2person statistics, found {} records", statPage.stream().toList().size());

        return statResponseHelper.fillStatPerson2PersonListResponse(statPage);
    }

    @Override
    public StatPhysicalCashOutListResponse getPhysicalCashOutStatistics(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start get physical cash out statistics for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        Pageable pageable = getPageableConfig(searchCriteria);
        Page<StatPhysicalCashOutEntity> statPage = statPhysicalCashOutRepositoryService.findAllWithSpecification(searchCriteria, pageable);
        log.info("finish get physical cash out statistics, found {} records", statPage.stream().toList().size());
        return statResponseHelper.fillStatPhysicalCashOutListResponse(statPage);
    }

    private Pageable getPageableConfig(Map<String, String> searchCriteria) {
        int page = Integer.parseInt(searchCriteria.getOrDefault("page", "0"));
        int size = Integer.parseInt(searchCriteria.getOrDefault("size", "10"));
        String orderBy = searchCriteria.getOrDefault("orderBy", "id");
        String sort = searchCriteria.getOrDefault("sort", "desc");
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, orderBy));
    }

}
