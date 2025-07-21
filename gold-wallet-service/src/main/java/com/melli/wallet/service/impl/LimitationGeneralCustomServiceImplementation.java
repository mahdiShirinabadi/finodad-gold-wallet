package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.LimitationGeneralCustomDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.LimitationGeneralCustomRepository;
import com.melli.wallet.domain.response.limitation.GeneralCustomLimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.LimitationGeneralCustomService;
import com.melli.wallet.service.LimitationGeneralService;
import com.melli.wallet.service.SettingGeneralService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Class Name: SettingGeneralCustomServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class LimitationGeneralCustomServiceImplementation implements LimitationGeneralCustomService {

    private final LimitationGeneralCustomRepository limitationGeneralCustomRepository;
    private final LimitationGeneralService limitationGeneralService;
    private final Helper helper;
    private final SettingGeneralService settingGeneralService;

    @Override
    public String getSetting(ChannelEntity channelEntity, String settingGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        LimitationGeneralEntity limitationGeneralEntity = limitationGeneralService.getSetting(settingGeneralName);


        if (limitationGeneralEntity == null) {
            log.error("limitationGeneralEntity with name ({}) not exist", settingGeneralName);
            throw new InternalServiceException("limitationGeneralEntity with name not exist", StatusService.LIMITATION_NOT_FOUND, HttpStatus.OK);
        }

        LimitationGeneralCustomDTO limitationGeneralCustomDTO = LimitationGeneralCustomDTO.builder().
                settingGeneralEntityId(String.valueOf(limitationGeneralEntity.getId()))
                .channelEntityId(String.valueOf(channelEntity.getId()))
                .walletLevelEntityId(String.valueOf(walletLevelEntity.getId()))
                .walletAccountTypeEntityId(String.valueOf(walletAccountTypeEntity.getId()))
                .walletAccountCurrencyEntityId(String.valueOf(walletAccountCurrencyEntity.getId()))
                .walletTypeEntityId(String.valueOf(walletTypeEntity.getId()))
                .build();

        List<LimitationGeneralCustomEntity> settingGeneralCustomEntityList = getSetting(limitationGeneralCustomDTO);
        if(CollectionUtils.isEmpty(settingGeneralCustomEntityList)){
            log.info("settingGeneralCustomEntityList is empty and return general setting value for setting ({}) is ({})", settingGeneralName, limitationGeneralEntity.getValue());
            return limitationGeneralEntity.getValue();
        }else if(settingGeneralCustomEntityList.size() == 1){
            log.info("settingGeneralCustomEntityList is exist and return value for setting ({}) is ({}), id for record table ({})", settingGeneralName, settingGeneralCustomEntityList.getFirst().getValue(), settingGeneralCustomEntityList.getFirst().getId());
            return settingGeneralCustomEntityList.getFirst().getValue();
        }else{
            log.error("there are multi setting for parameter ({}) and count row ({})", limitationGeneralCustomDTO, settingGeneralCustomEntityList.size());
            throw new InternalServiceException("customSetting is more than one record", StatusService.SETTING_MORE_THAN_ONE_RECORD, HttpStatus.OK);
        }
    }

    @Override
    public void update(ChannelEntity channelEntity, String limitationGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity, String value, String additionalData) throws InternalServiceException {

        LimitationGeneralEntity limitationGeneralEntity = limitationGeneralService.getSetting(limitationGeneralName);

        if (limitationGeneralEntity == null) {
            log.error("limitationGeneralEntity with name ({}) not exist", limitationGeneralName);
            throw new InternalServiceException("General limitation with this name not found", StatusService.LIMITATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        LimitationGeneralCustomDTO limitationGeneralCustomDTO = LimitationGeneralCustomDTO.builder().
                settingGeneralEntityId(String.valueOf(limitationGeneralEntity.getId()))
                .channelEntityId(String.valueOf(channelEntity.getId()))
                .walletLevelEntityId(String.valueOf(walletLevelEntity.getId()))
                .walletAccountTypeEntityId(String.valueOf(walletAccountTypeEntity.getId()))
                .walletAccountCurrencyEntityId(String.valueOf(walletAccountCurrencyEntity.getId()))
                .walletTypeEntityId(String.valueOf(walletTypeEntity.getId()))
                .build();

        List<LimitationGeneralCustomEntity> settingGeneralCustomEntityList = getSetting(limitationGeneralCustomDTO);

        if(CollectionUtils.isEmpty(settingGeneralCustomEntityList)){
            log.error("limitationGeneralCustomEntity with parameters ({}) not found", limitationGeneralCustomDTO);
            throw new InternalServiceException("Custom limitation with these parameters not found", StatusService.LIMITATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }else if(settingGeneralCustomEntityList.size() == 1){
            LimitationGeneralCustomEntity existingEntity = settingGeneralCustomEntityList.getFirst();
            
            // Set end time for current record
            existingEntity.setEndTime(new Date());
            limitationGeneralCustomRepository.save(existingEntity);
            
            // Create new record with updated values
            LimitationGeneralCustomEntity newEntity = new LimitationGeneralCustomEntity();
            newEntity.setLimitationGeneralEntity(limitationGeneralEntity);
            newEntity.setWalletLevelEntity(walletLevelEntity);
            newEntity.setWalletAccountTypeEntity(walletAccountTypeEntity);
            newEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyEntity);
            newEntity.setWalletTypeEntity(walletTypeEntity);
            newEntity.setChannelEntity(channelEntity);
            newEntity.setValue(value);
            newEntity.setAdditionalData(additionalData);
            newEntity.setCreatedBy(channelEntity.getUsername());
            newEntity.setCreatedAt(new Date());
            
            limitationGeneralCustomRepository.save(newEntity);
            log.info("limitationGeneralCustomEntity updated successfully for limitation ({})", limitationGeneralName);
        }else{
            log.error("there are multi setting for parameter ({}) and count row ({})", limitationGeneralCustomDTO, settingGeneralCustomEntityList.size());
            throw new InternalServiceException("Multiple custom limitation records found with these parameters", StatusService.SETTING_MORE_THAN_ONE_RECORD, HttpStatus.CONFLICT);
        }
    }

     //TODO test a lot
    @Override
    public void create(ChannelEntity channelEntity, Long settingGeneralId, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity, String value, String additionalData) throws InternalServiceException {

        LimitationGeneralEntity limitationGeneralEntity = limitationGeneralService.getById(settingGeneralId);

        if (limitationGeneralEntity == null) {
            log.error("settingGeneralEntity with id ({}) not exist", settingGeneralId);
            throw new InternalServiceException("settingGeneralEntity with name not exist", StatusService.SETTING_NOT_FOUND, HttpStatus.OK);
        }

        LimitationGeneralCustomDTO limitationGeneralCustomDTO = LimitationGeneralCustomDTO.builder().
                settingGeneralEntityId(String.valueOf(limitationGeneralEntity.getId()))
                .channelEntityId(String.valueOf(channelEntity.getId()))
                .walletLevelEntityId(String.valueOf(walletLevelEntity.getId()))
                .walletAccountTypeEntityId(String.valueOf(walletAccountTypeEntity.getId()))
                .walletAccountCurrencyEntityId(String.valueOf(walletAccountCurrencyEntity.getId()))
                .walletTypeEntityId(String.valueOf(walletTypeEntity.getId()))
                .build();

        List<LimitationGeneralCustomEntity> settingGeneralCustomEntityList = getSetting(limitationGeneralCustomDTO);

        if(CollectionUtils.isEmpty(settingGeneralCustomEntityList)){
            log.info("settingGeneralCustomEntityList is empty and generate setting value for setting ({}) is ({})", settingGeneralId, limitationGeneralEntity.getValue());
            LimitationGeneralCustomEntity settingGeneralCustomEntity = new LimitationGeneralCustomEntity();
            settingGeneralCustomEntity.setLimitationGeneralEntity(limitationGeneralEntity);
            settingGeneralCustomEntity.setWalletLevelEntity(walletLevelEntity);
            settingGeneralCustomEntity.setWalletAccountTypeEntity(walletAccountTypeEntity);
            settingGeneralCustomEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyEntity);
            settingGeneralCustomEntity.setWalletTypeEntity(walletTypeEntity);
            settingGeneralCustomEntity.setChannelEntity(channelEntity);
            settingGeneralCustomEntity.setValue(value);
            settingGeneralCustomEntity.setAdditionalData(additionalData);
            settingGeneralCustomEntity.setCreatedBy(channelEntity.getUsername());
            settingGeneralCustomEntity.setCreatedAt(new Date());
            limitationGeneralCustomRepository.save(settingGeneralCustomEntity);
        }else{
            log.info("there are multi setting for parameter ({}) and count row ({}) and start set EndTime for allRecord", limitationGeneralCustomDTO, settingGeneralCustomEntityList.size());
            for(LimitationGeneralCustomEntity row: settingGeneralCustomEntityList){
                row.setUpdatedAt(new Date());
                row.setUpdatedBy(channelEntity.getUsername());
                row.setEndTime(new Date());
                limitationGeneralCustomRepository.save(row);
            }

            LimitationGeneralCustomEntity settingGeneralCustomEntity = new LimitationGeneralCustomEntity();
            settingGeneralCustomEntity.setLimitationGeneralEntity(limitationGeneralEntity);
            settingGeneralCustomEntity.setWalletLevelEntity(walletLevelEntity);
            settingGeneralCustomEntity.setWalletAccountTypeEntity(walletAccountTypeEntity);
            settingGeneralCustomEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyEntity);
            settingGeneralCustomEntity.setWalletTypeEntity(walletTypeEntity);
            settingGeneralCustomEntity.setChannelEntity(channelEntity);
            settingGeneralCustomEntity.setValue(value);
            settingGeneralCustomEntity.setAdditionalData(additionalData);
            settingGeneralCustomEntity.setCreatedBy(channelEntity.getUsername());
            settingGeneralCustomEntity.setCreatedAt(new Date());
            limitationGeneralCustomRepository.save(settingGeneralCustomEntity);

        }
    }

    public List<LimitationGeneralCustomEntity> getSetting(LimitationGeneralCustomDTO limitationGeneralCustomDTO) {
        Pageable pageRequest = helper.getPageableConfig(settingGeneralService, 0,20);
        log.info("start find settingCustomDTO with data ({})", limitationGeneralCustomDTO);
        List<LimitationGeneralCustomEntity> settingGeneralCustomEntityList = limitationGeneralCustomRepository.findAll(predict(limitationGeneralCustomDTO), pageRequest).getContent();
        log.info("end find settingCustomDTO with data ({}) and countRecord ({})", limitationGeneralCustomDTO, settingGeneralCustomEntityList.size());
        return settingGeneralCustomEntityList;
    }


    private Specification<LimitationGeneralCustomEntity> predict(LimitationGeneralCustomDTO limitationGeneralCustomDTO){

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("limitationGeneralEntity").get("id"), limitationGeneralCustomDTO.getSettingGeneralEntityId()));
            predicates.add(criteriaBuilder.isNull(root.get("endTime")));

            if(StringUtils.hasText(limitationGeneralCustomDTO.getChannelEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("channelEntity").get("id"), limitationGeneralCustomDTO.getChannelEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("channelEntity")));
            }

            if(StringUtils.hasText(limitationGeneralCustomDTO.getWalletLevelEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletLevelEntity").get("id"), limitationGeneralCustomDTO.getWalletLevelEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletLevelEntity")));
            }

            if(StringUtils.hasText(limitationGeneralCustomDTO.getWalletAccountTypeEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletAccountTypeEntity").get("id"), limitationGeneralCustomDTO.getWalletAccountTypeEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletAccountTypeEntity")));
            }

            if(StringUtils.hasText(limitationGeneralCustomDTO.getWalletAccountCurrencyEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletAccountCurrencyEntity").get("id"), limitationGeneralCustomDTO.getWalletAccountCurrencyEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletAccountCurrencyEntity")));
            }

            if(StringUtils.hasText(limitationGeneralCustomDTO.getWalletTypeEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletTypeEntity").get("id"), limitationGeneralCustomDTO.getWalletTypeEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletTypeEntity")));
            }


            query.orderBy(criteriaBuilder.desc(root.get("id")));


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<LimitationGeneralCustomEntity> getSetting(LimitationGeneralEntity limitationGeneralEntity) {
        return limitationGeneralCustomRepository.findByLimitationGeneralEntityAndEndTimeIsNull(limitationGeneralEntity);
    }

    @Override
    public GeneralCustomLimitationListResponse getGeneralCustomLimitationList(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException {
        log.info("start find all GeneralCustomLimitation for username ({}), mapParameter ({})",
                channelEntity.getUsername(), Utility.mapToJsonOrNull(mapParameter));
        if (mapParameter == null) {
            mapParameter = new HashMap<>();
        }
        Pageable pageRequest = getPageableConfig(
                settingGeneralService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        Page<LimitationGeneralCustomEntity> limitationGeneralCustomEntityPage = limitationGeneralCustomRepository.findAll(getPredicate(mapParameter), pageRequest);
        return helper.fillGeneralCustomLimitationListResponse(limitationGeneralCustomEntityPage);
    }

    public Specification<LimitationGeneralCustomEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("desc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<LimitationGeneralCustomEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }

        if (StringUtils.hasText(searchCriteria.get("limitationGeneralId"))) {
            predicates.add(criteriaBuilder.equal(root.get("limitationGeneralEntity").get("id"), searchCriteria.get("limitationGeneralId")));
        }

        if (StringUtils.hasText(searchCriteria.get("walletLevelId"))) {
            predicates.add(criteriaBuilder.equal(root.get("walletLevelEntity").get("id"), searchCriteria.get("walletLevelId")));
        }

        if (StringUtils.hasText(searchCriteria.get("walletAccountTypeId"))) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountTypeEntity").get("id"), searchCriteria.get("walletAccountTypeId")));
        }

        if (StringUtils.hasText(searchCriteria.get("walletAccountCurrencyId"))) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountCurrencyEntity").get("id"), searchCriteria.get("walletAccountCurrencyId")));
        }

        if (StringUtils.hasText(searchCriteria.get("walletTypeId"))) {
            predicates.add(criteriaBuilder.equal(root.get("walletTypeEntity").get("id"), searchCriteria.get("walletTypeId")));
        }

        if (StringUtils.hasText(searchCriteria.get("channelId"))) {
            predicates.add(criteriaBuilder.equal(root.get("channelEntity").get("id"), searchCriteria.get("channelId")));
        }

        // Always filter by endTime is null (active records only)
        predicates.add(criteriaBuilder.isNull(root.get("endTime")));

        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<LimitationGeneralCustomEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
