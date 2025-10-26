package com.melli.wallet.utils;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.PanelChannelObject;
import com.melli.wallet.domain.response.channel.ChannelObject;
import com.melli.wallet.domain.response.collateral.*;
import com.melli.wallet.domain.response.limitation.GeneralCustomLimitationObject;
import com.melli.wallet.domain.response.limitation.GeneralLimitationObject;
import com.melli.wallet.domain.response.purchase.MerchantObject;
import com.melli.wallet.domain.response.transaction.ReportTransactionObject;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.panel.PanelResourceObject;
import com.melli.wallet.domain.response.panel.PanelRoleObject;
import com.melli.wallet.domain.slave.entity.*;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Component
@Log4j2
public class SubHelper {

    public static final String FORMAT_DATE_RESPONSE = "yyyy/MM/dd HH:mm:ss";

    public static WalletAccountTypeObject convertWalletAccountEntityToObject(WalletAccountTypeEntity entity){
        WalletAccountTypeObject walletAccountTypeObject = new WalletAccountTypeObject();
        walletAccountTypeObject.setId(String.valueOf(entity.getId()));
        walletAccountTypeObject.setName(entity.getName());
        walletAccountTypeObject.setAdditionalData(entity.getAdditionalData());
        walletAccountTypeObject.setDescription(entity.getDescription());
        return walletAccountTypeObject;
    }

    public static WalletAccountCurrencyObject convertWalletAccountCurrencyEntityToObject(WalletAccountCurrencyEntity entity){
        WalletAccountCurrencyObject walletAccountCurrencyObject = new WalletAccountCurrencyObject();
        walletAccountCurrencyObject.setId(String.valueOf(entity.getId()));
        walletAccountCurrencyObject.setName(entity.getName());
        walletAccountCurrencyObject.setSuffix(entity.getSuffix());
        walletAccountCurrencyObject.setAdditionalData(entity.getAdditionalData());
        walletAccountCurrencyObject.setDescription(entity.getDescription());
        return walletAccountCurrencyObject;
    }

    public static CollateralCreateTrackObject convertToCreateCollateralTrackResponse(CreateCollateralRequestEntity createCollateralRequestEntity, StatusRepositoryService statusRepositoryService){
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(createCollateralRequestEntity.getResult()));
        CollateralCreateTrackObject response = new CollateralCreateTrackObject();
        response.setUniqueIdentifier(createCollateralRequestEntity.getRrnEntity().getUuid());
        response.setCollateralCode(createCollateralRequestEntity.getCode());
        response.setQuantity((createCollateralRequestEntity.getQuantity().stripTrailingZeros().toPlainString()));
        response.setFinalQuantityBlock((createCollateralRequestEntity.getFinalBlockQuantity().stripTrailingZeros().toPlainString()));
        response.setCommission((createCollateralRequestEntity.getCommission().stripTrailingZeros().toPlainString()));
        response.setNationalCode(createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        response.setStatus(createCollateralRequestEntity.getCollateralStatusEnum().toString());
        response.setStatusDescription(createCollateralRequestEntity.getCollateralStatusEnum().toString());
        response.setAdditionalData(createCollateralRequestEntity.getAdditionalData());
        response.setCurrency(createCollateralRequestEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity().getName());
        response.setWalletAccountNumber(createCollateralRequestEntity.getWalletAccountEntity().getAccountNumber());
        response.setChannelName(createCollateralRequestEntity.getChannel().getUsername());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, createCollateralRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(createCollateralRequestEntity.getCreatedAt().getTime());
        response.setResult(String.valueOf(createCollateralRequestEntity.getResult()));
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        return response;
    }

    public static CollateralReleaseTrackObject convertToCollateralRelease(ReleaseCollateralRequestEntity releaseCollateralRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(releaseCollateralRequestEntity.getResult()));
        CollateralReleaseTrackObject trackObject = new CollateralReleaseTrackObject();
        trackObject.setChannelName(releaseCollateralRequestEntity.getChannel().getUsername());
        trackObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, releaseCollateralRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        trackObject.setCreateTimeTimestamp(releaseCollateralRequestEntity.getCreatedAt().getTime());
        trackObject.setQuantity((releaseCollateralRequestEntity.getQuantity().stripTrailingZeros().toPlainString()));
        trackObject.setResult(String.valueOf(releaseCollateralRequestEntity.getResult()));
        trackObject.setCommission((releaseCollateralRequestEntity.getCommission().stripTrailingZeros().toPlainString()));
        trackObject.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        return trackObject;
    }

    public static CollateralIncreaseTrackObject convertToCollateralIncrease(IncreaseCollateralRequestEntity increaseCollateralRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(increaseCollateralRequestEntity.getResult()));
        CollateralIncreaseTrackObject trackObject = new CollateralIncreaseTrackObject();
        trackObject.setId(String.valueOf(increaseCollateralRequestEntity.getId()));
        trackObject.setChannelName(increaseCollateralRequestEntity.getChannel().getUsername());
        trackObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, increaseCollateralRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        trackObject.setCreateTimeTimestamp(increaseCollateralRequestEntity.getCreatedAt().getTime());
        trackObject.setQuantity((increaseCollateralRequestEntity.getQuantity().stripTrailingZeros().toPlainString()));
        trackObject.setResult(String.valueOf(increaseCollateralRequestEntity.getResult()));
        trackObject.setCommission((increaseCollateralRequestEntity.getCommission().stripTrailingZeros().toPlainString()));
        trackObject.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        return trackObject;
    }

    public static PanelChannelObject fillPanelChannelObject(ReportChannelEntity channelEntity) {
        PanelChannelObject panelChannelObject = new PanelChannelObject();
        panelChannelObject.setId(String.valueOf(channelEntity.getId()));
        panelChannelObject.setIp(channelEntity.getIp());
        panelChannelObject.setMobile(channelEntity.getMobile());
        panelChannelObject.setUsername(channelEntity.getUsername());
        panelChannelObject.setFirstName(channelEntity.getFirstName());
        panelChannelObject.setLastName(channelEntity.getLastName());
        panelChannelObject.setPublicKey(channelEntity.getPublicKey());
        panelChannelObject.setStatus(String.valueOf(channelEntity.getStatus()));
        panelChannelObject.setCreateBy(channelEntity.getCreatedBy());
        panelChannelObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, channelEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        panelChannelObject.setUpdateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, channelEntity.getUpdatedAt(), FORMAT_DATE_RESPONSE, false));
        panelChannelObject.setUpdateBy(channelEntity.getUpdatedBy());
        panelChannelObject.setTrust(channelEntity.getTrust());
        panelChannelObject.setSign(channelEntity.getSign());
        return panelChannelObject;
    }

    public static MerchantObject convertMerchantEntityToMerchantObject(MerchantEntity merchantEntity) {
        return new MerchantObject(String.valueOf(merchantEntity.getId()), merchantEntity.getName(), merchantEntity.getLogo(), String.valueOf(merchantEntity.getStatus()));
    }

    public static CollateralObject convertCollateralEntityToCollateralObject(CollateralEntity collateralEntity) {
        return new CollateralObject(String.valueOf(collateralEntity.getId()), collateralEntity.getName(), collateralEntity.getLogo(), String.valueOf(collateralEntity.getStatus()),  collateralEntity.getIban());
    }

    public static String prettyBalance(BigDecimal num){
        DecimalFormat df = new DecimalFormat("0.#####");
        return df.format(num);
    }

    public static ChannelObject convertChannelEntityToChannelObject(ChannelEntity channelEntity) {
        ChannelObject channelObject = new ChannelObject();
        channelObject.setFirstName(channelEntity.getFirstName());
        channelObject.setLastName(channelEntity.getLastName());
        channelObject.setUsername(channelEntity.getUsername());
        channelObject.setMobile(channelEntity.getMobile());
        return channelObject;
    }

    public static PanelRoleObject fillChannelRoleListObject(ReportChannelRoleEntity channelRoleEntity) {
        PanelRoleObject panelOperatorRoleObject = new PanelRoleObject();
        ReportRoleEntity roleEntity = channelRoleEntity.getRoleEntity();
        panelOperatorRoleObject.setId(String.valueOf(roleEntity.getId()));
        panelOperatorRoleObject.setName(roleEntity.getName());
        panelOperatorRoleObject.setCreatedTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, roleEntity.getCreatedAt(), DateUtils.DEFAULT_DATE_TIME_FORMAT, false));
        panelOperatorRoleObject.setCreatedBy(roleEntity.getCreatedBy());
        panelOperatorRoleObject.setUpdatedTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, roleEntity.getUpdatedAt(), DateUtils.DEFAULT_DATE_TIME_FORMAT, false));
        panelOperatorRoleObject.setUpdatedBy(roleEntity.getUpdatedBy());
        List<PanelResourceObject> resourceObjects = roleEntity.getResources().stream().map(SubHelper::fillPanelResourceListObject).toList();
        panelOperatorRoleObject.setResources(resourceObjects);
        return panelOperatorRoleObject;
    }

    public static PanelResourceObject fillPanelResourceListObject(ReportResourceEntity resourceEntity) {
        PanelResourceObject panelOperatorRoleObject = new PanelResourceObject();
        panelOperatorRoleObject.setId(String.valueOf(resourceEntity.getId()));
        panelOperatorRoleObject.setName(resourceEntity.getName());
        panelOperatorRoleObject.setCreatedAt(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, resourceEntity.getCreatedAt(), DateUtils.DEFAULT_DATE_FORMAT, false));
        panelOperatorRoleObject.setCreatedBy(resourceEntity.getCreatedBy());
        panelOperatorRoleObject.setUpdatedAt(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, resourceEntity.getUpdatedAt(), DateUtils.DEFAULT_DATE_FORMAT, false));
        panelOperatorRoleObject.setUpdatedBy(resourceEntity.getUpdatedBy());
        return panelOperatorRoleObject;
    }

    public static GeneralCustomLimitationObject fillGeneralCustomLimitationObject(LimitationGeneralCustomEntity limitationGeneralCustomEntity) {
        GeneralCustomLimitationObject object = new GeneralCustomLimitationObject();
        object.setId(String.valueOf(limitationGeneralCustomEntity.getId()));
        object.setLimitationGeneralId(String.valueOf(limitationGeneralCustomEntity.getLimitationGeneralEntity().getId()));
        object.setLimitationGeneralName(limitationGeneralCustomEntity.getLimitationGeneralEntity().getName());
        object.setGeneralLimitationObject(fillGeneralLimitationObject(limitationGeneralCustomEntity.getLimitationGeneralEntity()));
        object.setValue(limitationGeneralCustomEntity.getValue());
        object.setAdditionalData(limitationGeneralCustomEntity.getAdditionalData());
        object.setWalletLevelId(limitationGeneralCustomEntity.getWalletLevelEntity() != null ?
                String.valueOf(limitationGeneralCustomEntity.getWalletLevelEntity().getId()) : null);
        object.setWalletLevelName(limitationGeneralCustomEntity.getWalletLevelEntity() != null ?
                limitationGeneralCustomEntity.getWalletLevelEntity().getName() : null);
        object.setWalletAccountTypeId(limitationGeneralCustomEntity.getWalletAccountTypeEntity() != null ?
                String.valueOf(limitationGeneralCustomEntity.getWalletAccountTypeEntity().getId()) : null);
        object.setWalletAccountTypeName(limitationGeneralCustomEntity.getWalletAccountTypeEntity() != null ?
                limitationGeneralCustomEntity.getWalletAccountTypeEntity().getName() : null);
        object.setWalletAccountCurrencyId(limitationGeneralCustomEntity.getWalletAccountCurrencyEntity() != null ?
                String.valueOf(limitationGeneralCustomEntity.getWalletAccountCurrencyEntity().getId()) : null);
        object.setWalletAccountCurrencyName(limitationGeneralCustomEntity.getWalletAccountCurrencyEntity() != null ?
                limitationGeneralCustomEntity.getWalletAccountCurrencyEntity().getName() : null);
        object.setWalletTypeId(limitationGeneralCustomEntity.getWalletTypeEntity() != null ?
                String.valueOf(limitationGeneralCustomEntity.getWalletTypeEntity().getId()) : null);
        object.setWalletTypeName(limitationGeneralCustomEntity.getWalletTypeEntity() != null ?
                limitationGeneralCustomEntity.getWalletTypeEntity().getName() : null);
        object.setChannelId(limitationGeneralCustomEntity.getChannelEntity() != null ?
                String.valueOf(limitationGeneralCustomEntity.getChannelEntity().getId()) : null);
        object.setChannelName(limitationGeneralCustomEntity.getChannelEntity() != null ?
                limitationGeneralCustomEntity.getChannelEntity().getUsername() : null);
        object.setCreateTime(limitationGeneralCustomEntity.getCreatedAt() != null ?
                DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralCustomEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false) : null);
        object.setCreateBy(limitationGeneralCustomEntity.getCreatedBy());
        object.setEndTime(limitationGeneralCustomEntity.getEndTime() != null ?
                DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralCustomEntity.getEndTime(), FORMAT_DATE_RESPONSE, false) : null);
        return object;
    }

    public static GeneralLimitationObject fillGeneralLimitationObject(LimitationGeneralEntity limitationGeneralEntity) {
        GeneralLimitationObject object = new GeneralLimitationObject();
        object.setId(String.valueOf(limitationGeneralEntity.getId()));
        object.setName(limitationGeneralEntity.getName());
        object.setValue(limitationGeneralEntity.getValue());
        object.setPattern(limitationGeneralEntity.getPattern());
        object.setAdditionalData(limitationGeneralEntity.getAdditionalData());
        object.setCreateTime(limitationGeneralEntity.getCreatedAt() != null ?
                DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false) : null);
        object.setCreateBy(limitationGeneralEntity.getCreatedBy());
        object.setUpdateTime(limitationGeneralEntity.getUpdatedAt() != null ?
                DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralEntity.getUpdatedAt(), FORMAT_DATE_RESPONSE, false) : null);
        object.setUpdateBy(limitationGeneralEntity.getUpdatedBy());
        return object;
    }

    public static ReportTransactionObject convertToReportStatementObject(ReportTransactionEntity reportTransactionEntity) {
        ReportTransactionObject statementObject = new ReportTransactionObject();
        statementObject.setNationalCode(reportTransactionEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        statementObject.setId(String.valueOf(reportTransactionEntity.getId()));
        statementObject.setAccountNumber(reportTransactionEntity.getWalletAccountEntity().getAccountNumber());
        statementObject.setType(reportTransactionEntity.getType());
        statementObject.setUniqueIdentifier(reportTransactionEntity.getRrnEntity().getUuid());
        statementObject.setCurrency(reportTransactionEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity().getName());
        statementObject.setQuantity(reportTransactionEntity.getAmount().stripTrailingZeros().toPlainString());
        statementObject.setBalance(reportTransactionEntity.getAvailableBalance().stripTrailingZeros().toPlainString());
        statementObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, reportTransactionEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        return statementObject;
    }
}
