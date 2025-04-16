package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.SettingGeneralCustomDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.SettingGeneralCustomRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.SettingGeneralCustomService;
import com.melli.wallet.service.SettingGeneralService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.utils.Helper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class Name: SettingGeneralCustomServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class SettingGeneralCustomServiceImplementation implements SettingGeneralCustomService {

    private final SettingGeneralCustomRepository settingGeneralCustomRepository;
    private final SettingGeneralService settingGeneralService;
    private final Helper helper;

    @Override
    public String getSetting(ChannelEntity channelEntity, String settingGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        SettingGeneralEntity settingGeneralEntity = settingGeneralService.getSetting(settingGeneralName);


        if (settingGeneralEntity == null) {
            log.error("settingGeneralEntity with name ({}) not exist", settingGeneralName);
            throw new InternalServiceException("settingGeneralEntity with name not exist", StatusService.SETTING_NOT_FOUND, HttpStatus.OK);
        }

        SettingGeneralCustomDTO settingGeneralCustomDTO = SettingGeneralCustomDTO.builder().
                settingGeneralEntityId(String.valueOf(settingGeneralEntity.getId()))
                .channelEntityId(String.valueOf(channelEntity.getId()))
                .walletLevelEntityId(String.valueOf(walletLevelEntity.getId()))
                .walletAccountTypeEntityId(String.valueOf(walletAccountTypeEntity.getId()))
                .walletAccountCurrencyEntityId(String.valueOf(walletAccountCurrencyEntity.getId()))
                .walletTypeEntityId(String.valueOf(walletTypeEntity.getId()))
                .build();

        List<SettingGeneralCustomEntity> settingGeneralCustomEntityList = getSetting(settingGeneralCustomDTO);
        if(CollectionUtils.isEmpty(settingGeneralCustomEntityList)){
            return settingGeneralEntity.getValue();
        }else if(settingGeneralCustomEntityList.size() == 1){
            return settingGeneralCustomEntityList.getFirst().getValue();
        }else{
            log.error("there are multi setting for parameter ({}) and count row ({})", settingGeneralCustomDTO, settingGeneralCustomEntityList.size());
            throw new InternalServiceException("customSetting is more than one record", StatusService.SETTING_MORE_THAN_ONE_RECORD, HttpStatus.OK);
        }
    }

    public List<SettingGeneralCustomEntity> getSetting(SettingGeneralCustomDTO settingGeneralCustomDTO) {
        Pageable pageRequest = helper.getPageableConfig(settingGeneralService, 0,2);
        log.info("start find settingCustomDTO with data ({})", settingGeneralCustomDTO);
        List<SettingGeneralCustomEntity> settingGeneralCustomEntityList = settingGeneralCustomRepository.findAll(predict(settingGeneralCustomDTO), pageRequest).getContent();
        log.info("end find settingCustomDTO with data ({}) and countRecord ({})", settingGeneralCustomDTO, settingGeneralCustomEntityList.size());
        return settingGeneralCustomEntityList;
    }


    private Specification<SettingGeneralCustomEntity> predict(SettingGeneralCustomDTO settingGeneralCustomDTO){

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("settingGeneralEntity").get("id"),settingGeneralCustomDTO.getSettingGeneralEntityId()));
            predicates.add(criteriaBuilder.isNull(root.get("endTime")));

            if(StringUtils.hasText(settingGeneralCustomDTO.getChannelEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("channelEntity").get("id"),settingGeneralCustomDTO.getChannelEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("channelEntity")));
            }

            if(StringUtils.hasText(settingGeneralCustomDTO.getWalletLevelEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletLevelEntity").get("id"),settingGeneralCustomDTO.getWalletLevelEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletLevelEntity")));
            }

            if(StringUtils.hasText(settingGeneralCustomDTO.getWalletAccountTypeEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletAccountTypeEntity").get("id"),settingGeneralCustomDTO.getWalletAccountTypeEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletAccountTypeEntity")));
            }

            if(StringUtils.hasText(settingGeneralCustomDTO.getWalletAccountCurrencyEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletAccountCurrencyEntity").get("id"),settingGeneralCustomDTO.getWalletAccountCurrencyEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletAccountCurrencyEntity")));
            }

            if(StringUtils.hasText(settingGeneralCustomDTO.getWalletTypeEntityId())){
                predicates.add(criteriaBuilder.equal(root.get("walletTypeEntity").get("id"),settingGeneralCustomDTO.getWalletTypeEntityId()));
            }else{
                predicates.add(criteriaBuilder.isNull(root.get("walletTypeEntity")));
            }


            query.orderBy(criteriaBuilder.desc(root.get("id")));


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    @Override
    public void save(ChannelEntity channelEntity, SettingGeneralCustomEntity settingGeneralCustomEntity) throws InternalServiceException  {
        SettingGeneralCustomDTO settingGeneralCustomDTO = SettingGeneralCustomDTO.builder().
                settingGeneralEntityId(String.valueOf(settingGeneralCustomEntity.getSettingGeneralEntity().getId()))
                .channelEntityId(String.valueOf(settingGeneralCustomEntity.getChannelEntity().getId()))
                .walletLevelEntityId(String.valueOf(settingGeneralCustomEntity.getWalletLevelEntity().getId()))
                .walletAccountTypeEntityId(String.valueOf(settingGeneralCustomEntity.getWalletAccountTypeEntity().getId()))
                .walletAccountCurrencyEntityId(String.valueOf(settingGeneralCustomEntity.getWalletAccountCurrencyEntity().getId()))
                .walletTypeEntityId(String.valueOf(settingGeneralCustomEntity.getWalletTypeEntity().getId()))
                .build();

        List<SettingGeneralCustomEntity> settingGeneralCustomEntityList = getSetting(settingGeneralCustomDTO);
        if(settingGeneralCustomEntityList.size()> 1){
            log.error("custom settingGeneral with data ({}) exist in database with more than one record", settingGeneralCustomDTO);
            throw new InternalServiceException("customSetting is more than one record", StatusService.SETTING_MORE_THAN_ONE_RECORD, HttpStatus.OK);
        }

        for(SettingGeneralCustomEntity row: settingGeneralCustomEntityList){
            row.setUpdatedAt(new Date());
            row.setUpdatedBy(channelEntity.getUsername());
            row.setEndTime(new Date());
            settingGeneralCustomRepository.save(row);
        }

        settingGeneralCustomRepository.save(settingGeneralCustomEntity);

    }

    @Override
    public List<SettingGeneralCustomEntity> getSetting(SettingGeneralEntity settingGeneralEntity) {
        return settingGeneralCustomRepository.findBySettingGeneralEntityAndEndTimeIsNull(settingGeneralEntity);
    }
}
