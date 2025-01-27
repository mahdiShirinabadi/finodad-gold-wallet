package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.master.persistence.SettingGeneralCustomRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.SettingGeneralCustomService;
import com.melli.hub.service.SettingGeneralService;
import com.melli.hub.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
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

    @Override
    public String getSetting(ChannelEntity channelEntity, String settingGeneralName, WalletLevelEntity walletLevelEntity, WalletAccountTypeEntity walletAccountTypeEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        SettingGeneralEntity settingGeneralEntity = settingGeneralService.getSetting(settingGeneralName);

        if(settingGeneralEntity == null) {
            log.error("settingGeneralEntity with name ({}) not exist", settingGeneralName);
            throw new InternalServiceException("settingGeneralEntity with name not exist", StatusService.SETTING_NOT_FOUND, HttpStatus.OK);
        }

        log.info("start find all SettingGeneralCustomerEntity for name ({}) for channel ({}) where endTime is null", settingGeneralEntity.getName(), channelEntity.getUsername());
        List<SettingGeneralCustomEntity> settingGeneralCustomEntityList = settingGeneralCustomRepository.findBySettingGeneralEntityAndChannelEntityAndEndTimeIsNull(settingGeneralEntity, channelEntity);
        if (CollectionUtils.isEmpty(settingGeneralCustomEntityList)) {
            log.info("setting with name ({}) dont have a SettingGeneralCustomerEntity and return default value ({})", settingGeneralEntity.getName(), settingGeneralEntity.getValue());
            return settingGeneralEntity.getValue();
        }

        log.info("size settingGeneralCustomEntityList for setting with name ({}) is ({})", settingGeneralEntity.getName(), settingGeneralCustomEntityList.size());

        log.info("start to walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}),  walletTypeEntity ({}) in settingAccountGroupTypeList", walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
        List<SettingGeneralCustomEntity> settingGeneralCustomEntityListWithDetail = settingGeneralCustomEntityList.stream().filter(x -> x.getWalletAccountTypeEntity().getId() == walletAccountTypeEntity.getId() &&
                x.getWalletAccountCurrencyEntity().getId() == walletAccountCurrencyEntity.getId() && x.getWalletTypeEntity().getId() == walletTypeEntity.getId()).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(settingGeneralCustomEntityListWithDetail)) {
            log.info("exist walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({})  in settingAccountGroupTypeList", walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            return findSettingValuesFromList(settingGeneralCustomEntityListWithDetail, settingGeneralEntity, walletAccountCurrencyEntity, walletAccountTypeEntity, walletTypeEntity);
        }

        log.info("setting with name ({}) dont have a SettingAccountGroupType and start to find with another situation", settingGeneralEntity.getName());
        return findSettingValuesFromList(settingGeneralCustomEntityList, settingGeneralEntity, walletAccountCurrencyEntity, walletAccountTypeEntity, walletTypeEntity);
    }

    @Override
    public SettingGeneralCustomEntity getSetting(SettingGeneralEntity settingGeneralEntity) {
        return settingGeneralCustomRepository.findBySettingGeneralEntityAndEndTimeIsNull(settingGeneralEntity);
    }

    @Override
    public void save(SettingGeneralCustomEntity settingGeneralCustomEntity) {
        settingGeneralCustomRepository.save(settingGeneralCustomEntity);
    }


    private String findSettingValuesFromList(List<SettingGeneralCustomEntity> settingGeneralCustomEntityList, SettingGeneralEntity settingGeneralEntity, WalletAccountCurrencyEntity walletAccountCurrencyEntity, WalletAccountTypeEntity walletAccountTypeEntity
            , WalletTypeEntity walletTypeEntity) {

        log.info("size settingGeneralCustomEntityList for setting  name ({}) and is ({})", settingGeneralEntity.getName(), settingGeneralCustomEntityList.size());

        log.info("start to walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}),  walletTypeEntity ({}) in settingGeneralCustomEntityList",  walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
        //first privilege for dedicate accountGroup and accountType
        List<SettingGeneralCustomEntity> settingAccountGroupTypeAccountTypeList = settingGeneralCustomEntityList.stream().filter(x -> x.getWalletAccountTypeEntity().getId() == walletAccountTypeEntity.getId() &&
                x.getWalletAccountCurrencyEntity().getId() == walletAccountCurrencyEntity.getId() && x.getWalletTypeEntity().getId() == walletTypeEntity.getId()).collect(Collectors.toList());

        settingAccountGroupTypeAccountTypeList.sort((c1, c2) -> Long.compare(c2.getId(), c1.getId()));

        if (!settingAccountGroupTypeAccountTypeList.isEmpty()) {
            String value = settingAccountGroupTypeAccountTypeList.getFirst().getValue();
            log.info("success to find value ({}) for setting name ({}) walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}),  walletTypeEntity ({}) in settingAccountGroupTypeForMerchantAccountGroupIdList", value, settingGeneralEntity.getName(), walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            return value;
        }

        log.info("system can not find SettingGeneralEntity with name ({}) and return default value ({}) for walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}),  walletTypeEntity ({})", settingGeneralEntity.getName(), settingGeneralEntity.getValue(), walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
        return settingGeneralEntity.getValue();
    }

}
