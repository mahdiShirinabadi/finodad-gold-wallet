package com.melli.wallet.service.repository.impl;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.persistence.ChannelRepository;
import com.melli.wallet.domain.response.PanelChannelResponse;
import com.melli.wallet.domain.response.panel.PanelRoleListResponse;
import com.melli.wallet.domain.slave.entity.ReportChannelEntity;
import com.melli.wallet.domain.slave.entity.ReportChannelRoleEntity;
import com.melli.wallet.domain.slave.persistence.ReportChannelRepository;
import com.melli.wallet.domain.slave.persistence.ReportChannelRoleRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.ChannelRepositoryService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.utils.Helper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.melli.wallet.ConstantRedisName.CHANNEL_NAME_CACHE;


@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = CHANNEL_NAME_CACHE)
public class ChannelRepositoryServiceImplementation implements ChannelRepositoryService {


    private final ChannelRepository channelRepository;
    private final ReportChannelRepository reportChannelRepository;
    private final ReportChannelRoleRepository reportChannelRoleRepository;
    private final Helper helper;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;

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
            return new InternalServiceException("channel not found", StatusRepositoryService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        });
    }

    @Override
    @LogExecutionTime("checking findByUsername")
    public ChannelEntity findByUsername(String username) {
        return channelRepository.findByUsername(username);
    }

    @Override
    public void save(ChannelEntity channelEntity){
        channelRepository.save(channelEntity);
    }

    @Override
    public ChannelEntity saveChannel(ChannelEntity channelEntity) throws InternalServiceException {
        log.info("start save channel in channel service impl with parameters : userName ={} ,trust ={}, sign ={},name ={},publicKey ={},ip ={},status ={}, checkShahkar ={}",
                channelEntity.getUsername(), channelEntity.getTrust(), channelEntity.getSign(), channelEntity.getLastName(), channelEntity.getPublicKey(), channelEntity.getIp(), channelEntity.getStatus(), channelEntity.getCheckShahkar());
        if (findByUsername(channelEntity.getUsername()) != null) {
            log.error("in create channel service ,channel by channel name = {} exist", channelEntity.getUsername());
            throw new InternalServiceException("channel with username is duplicate", StatusRepositoryService.DUPLICATE_CHANNEL_NAME, HttpStatus.OK);
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
            return new InternalServiceException("channel not found", StatusRepositoryService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        });

        if (!CustomStringUtils.hasText(password)) {
            log.error("in change Password Channel service ,password not valid by channel name = {} ", channelId);
            throw new InternalServiceException("channel not found", StatusRepositoryService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        }
        channelEntity.setPassword(bcryptEncoder.encode(password));
        return channelRepository.save(channelEntity);
    }

    @Override
    public PanelRoleListResponse listChannelRoles(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException {
        log.info("Fetching all channel roles for operator: {}, mapParameter: {}", channelEntity.getUsername(), mapParameter);
        Map<String, String> searchParams = Optional.ofNullable(mapParameter).orElseGet(HashMap::new);
        if (!CustomStringUtils.hasText(searchParams.get("channelId"))) {
            log.error("channelId is empty");
            throw new InternalServiceException("channel Id is empty", StatusRepositoryService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        }
        Pageable pageRequest = helper.getPageableConfig(
                settingGeneralRepositoryService,
                Integer.parseInt(searchParams.getOrDefault("page", "0")),
                Integer.parseInt(searchParams.getOrDefault("size", "10"))
        );
        Specification<ReportChannelRoleEntity> specification = createSearchSpecification(searchParams);
        Page<ReportChannelRoleEntity> page = reportChannelRoleRepository.findAll(specification, pageRequest);
        return helper.fillChannelRoleListResponse(page);
    }

    private Specification<ReportChannelRoleEntity> createSearchSpecification(Map<String, String> mapParameter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (CustomStringUtils.hasText(mapParameter.get("channelId"))) {
                predicates.add(cb.equal(root.get("channelEntity").get("id"), mapParameter.get("channelId"))
                );
            }
            predicates.add(cb.isNull(root.get("deleteTime")));
            applySorting(query, mapParameter, cb, root);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void applySorting(CriteriaQuery<?> query,
                              Map<String, String> criteria,
                              CriteriaBuilder cb,
                              Root<ReportChannelRoleEntity> root) {

        String orderBy = criteria.getOrDefault("orderBy", "id");
        String sortDirection = criteria.getOrDefault("sort", "asc");

        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(cb.asc(root.get(orderBy)));
        } else {
            query.orderBy(cb.desc(root.get(orderBy)));
        }
    }

    @Override
    public PanelChannelResponse list(Map<String, String> mapParameter) throws InternalServiceException {
        log.info("start find all channel for mapParameter: {}", mapParameter);
        if (mapParameter == null) {
            mapParameter = new HashMap<>();
        }
        Pageable pageRequest = helper.getPageableConfig(
                settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        Specification<ReportChannelEntity> channelEntitySpecification = getProfileEntityPredicate(mapParameter);
        Page<ReportChannelEntity> channelEntityPage = reportChannelRepository.findAll(channelEntitySpecification, pageRequest);
        return helper.fillPanelChannelResponse(channelEntityPage);
    }

    private Specification<ReportChannelEntity> getProfileEntityPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ReportChannelEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CustomStringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        predicates.add(criteriaBuilder.isNull(root.get("deleteTime")));
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<ReportChannelEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}

