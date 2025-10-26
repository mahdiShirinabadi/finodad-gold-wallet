package com.melli.wallet.utils;

import com.melli.wallet.domain.master.entity.StatBuyEntity;
import com.melli.wallet.domain.master.entity.StatSellEntity;
import com.melli.wallet.domain.master.entity.StatWalletEntity;
import com.melli.wallet.domain.master.entity.StatPerson2PersonEntity;
import com.melli.wallet.domain.master.entity.StatPhysicalCashOutEntity;
import com.melli.wallet.domain.response.stat.StatBuyListResponse;
import com.melli.wallet.domain.response.stat.StatSellListResponse;
import com.melli.wallet.domain.response.stat.StatWalletListResponse;
import com.melli.wallet.domain.response.stat.StatPerson2PersonListResponse;
import com.melli.wallet.domain.response.stat.StatPhysicalCashOutListResponse;
import com.melli.wallet.domain.response.stat.StatBuyObject;
import com.melli.wallet.domain.response.stat.StatSellObject;
import com.melli.wallet.domain.response.stat.StatWalletObject;
import com.melli.wallet.domain.response.stat.StatPerson2PersonObject;
import com.melli.wallet.domain.response.stat.StatPhysicalCashOutObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Class Name: StatResponseHelper
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Helper class specifically for statistics response operations
 */
@Component
@Log4j2
public class StatResponseHelper {

    public StatBuyListResponse fillStatBuyListResponse(Page<StatBuyEntity> statBuyPage) {
        StatBuyListResponse response = new StatBuyListResponse();
        response.setStatBuyObjectList(statBuyPage.stream().map(this::convertStatBuyToObject).toList());
        response.setTotalElements(statBuyPage.getTotalElements());
        response.setTotalPages(statBuyPage.getTotalPages());
        response.setSize(statBuyPage.getSize());
        response.setNumber(statBuyPage.getNumberOfElements());
        return response;
    }

    public StatSellListResponse fillStatSellListResponse(Page<StatSellEntity> statSellPage) {
        StatSellListResponse response = new StatSellListResponse();
        response.setStatSellObjectList(statSellPage.stream().map(this::convertStatSellToObject).toList());
        response.setTotalElements(statSellPage.getTotalElements());
        response.setTotalPages(statSellPage.getTotalPages());
        response.setSize(statSellPage.getSize());
        response.setNumber(statSellPage.getNumberOfElements());
        return response;
    }

    public StatWalletListResponse fillStatWalletListResponse(Page<StatWalletEntity> statWalletPage) {
        StatWalletListResponse response = new StatWalletListResponse();
        response.setStatWalletObjectList(statWalletPage.stream().map(this::convertStatWalletToObject).toList());
        response.setTotalElements(statWalletPage.getTotalElements());
        response.setTotalPages(statWalletPage.getTotalPages());
        response.setSize(statWalletPage.getSize());
        response.setNumber(statWalletPage.getNumberOfElements());
        return response;
    }

    public StatPerson2PersonListResponse fillStatPerson2PersonListResponse(Page<StatPerson2PersonEntity> statPerson2PersonPage) {
        StatPerson2PersonListResponse response = new StatPerson2PersonListResponse();
        response.setStatPerson2PersonObjectList(statPerson2PersonPage.stream().map(this::convertStatPerson2PersonToObject).toList());
        response.setTotalElements(statPerson2PersonPage.getTotalElements());
        response.setTotalPages(statPerson2PersonPage.getTotalPages());
        response.setSize(statPerson2PersonPage.getSize());
        response.setNumber(statPerson2PersonPage.getNumberOfElements());
        return response;
    }

    public StatPhysicalCashOutListResponse fillStatPhysicalCashOutListResponse(Page<StatPhysicalCashOutEntity> statPhysicalCashOutPage) {
        StatPhysicalCashOutListResponse response = new StatPhysicalCashOutListResponse();
        response.setStatPhysicalCashOutObjectList(statPhysicalCashOutPage.stream().map(this::convertStatPhysicalCashOutToObject).toList());
        response.setTotalElements(statPhysicalCashOutPage.getTotalElements());
        response.setTotalPages(statPhysicalCashOutPage.getTotalPages());
        response.setSize(statPhysicalCashOutPage.getSize());
        response.setNumber(statPhysicalCashOutPage.getNumberOfElements());
        return response;
    }

    private StatBuyObject convertStatBuyToObject(StatBuyEntity statEntity) {
        StatBuyObject statObject = new StatBuyObject();
        statObject.setId(statEntity.getId());
        statObject.setChannelId(statEntity.getChannelId());
        statObject.setCurrencyId(statEntity.getCurrencyId());
        statObject.setMerchantId(statEntity.getMerchantId());
        statObject.setResult(statEntity.getResult());
        statObject.setCount(statEntity.getCount());
        statObject.setAmount(statEntity.getAmount());
        statObject.setPrice(statEntity.getPrice());
        statObject.setPersianCalcDate(statEntity.getPersianCalcDate());
        statObject.setGeorgianCalcDate(statEntity.getGeorgianCalcDate());
        statObject.setCreatedAt(statEntity.getCreatedAt());
        statObject.setUpdatedAt(statEntity.getUpdatedAt());
        return statObject;
    }

    private StatSellObject convertStatSellToObject(StatSellEntity statEntity) {
        StatSellObject statObject = new StatSellObject();
        statObject.setId(statEntity.getId());
        statObject.setChannelId(statEntity.getChannelId());
        statObject.setCurrencyId(statEntity.getCurrencyId());
        statObject.setMerchantId(statEntity.getMerchantId());
        statObject.setResult(statEntity.getResult());
        statObject.setCount(statEntity.getCount());
        statObject.setAmount(statEntity.getAmount());
        statObject.setPrice(statEntity.getPrice());
        statObject.setPersianCalcDate(statEntity.getPersianCalcDate());
        statObject.setGeorgianCalcDate(statEntity.getGeorgianCalcDate());
        statObject.setCreatedAt(statEntity.getCreatedAt());
        statObject.setUpdatedAt(statEntity.getUpdatedAt());
        return statObject;
    }

    private StatWalletObject convertStatWalletToObject(StatWalletEntity statEntity) {
        StatWalletObject statObject = new StatWalletObject();
        statObject.setId(statEntity.getId());
        statObject.setChannelId(statEntity.getChannelId());
        statObject.setCount(statEntity.getCount());
        statObject.setPersianCalcDate(statEntity.getPersianCalcDate());
        statObject.setGeorgianCalcDate(statEntity.getGeorgianCalcDate());
        statObject.setCreatedAt(statEntity.getCreatedAt());
        statObject.setUpdatedAt(statEntity.getUpdatedAt());
        return statObject;
    }

    private StatPerson2PersonObject convertStatPerson2PersonToObject(StatPerson2PersonEntity statEntity) {
        StatPerson2PersonObject statObject = new StatPerson2PersonObject();
        statObject.setId(statEntity.getId());
        statObject.setChannelId(statEntity.getChannelId());
        statObject.setCurrencyId(statEntity.getCurrencyId());
        statObject.setResult(statEntity.getResult());
        statObject.setCount(statEntity.getCount());
        statObject.setAmount(statEntity.getAmount());
        statObject.setPersianCalcDate(statEntity.getPersianCalcDate());
        statObject.setGeorgianCalcDate(statEntity.getGeorgianCalcDate());
        statObject.setCreatedAt(statEntity.getCreatedAt());
        statObject.setUpdatedAt(statEntity.getUpdatedAt());
        return statObject;
    }

    private StatPhysicalCashOutObject convertStatPhysicalCashOutToObject(StatPhysicalCashOutEntity statEntity) {
        StatPhysicalCashOutObject statObject = new StatPhysicalCashOutObject();
        statObject.setId(statEntity.getId());
        statObject.setChannelId(statEntity.getChannelId());
        statObject.setCurrencyId(statEntity.getCurrencyId());
        statObject.setResult(statEntity.getResult());
        statObject.setCount(statEntity.getCount());
        statObject.setAmount(statEntity.getAmount());
        statObject.setPersianCalcDate(statEntity.getPersianCalcDate());
        statObject.setGeorgianCalcDate(statEntity.getGeorgianCalcDate());
        statObject.setCreatedAt(statEntity.getCreatedAt());
        statObject.setUpdatedAt(statEntity.getUpdatedAt());
        return statObject;
    }
}
