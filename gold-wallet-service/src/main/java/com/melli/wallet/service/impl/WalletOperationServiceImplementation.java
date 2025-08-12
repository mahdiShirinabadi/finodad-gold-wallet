package com.melli.wallet.service.impl;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
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

import java.util.*;

/**
 * Class Name: WalletOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletOperationServiceImplementation implements WalletOperationalService {

    private final ShahkarInfoOperationService shahkarInfoOperationService;
    private final RedisLockService redisLockService;
    private final WalletService walletService;
    private final Helper helper;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final WalletLevelService walletLevelService;
    private final SettingGeneralService settingGeneralService;
    private final TransactionService transactionService;

    @Override
    public CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletTypeString, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException {
        if (channelEntity.getCheckShahkar() == ChannelService.TRUE || StringUtils.hasText(nationalCode)) {
            log.info("Start checking if nationalCode({}) and mobile({}) are related from shahkar or not ...", nationalCode, mobile);
            shahkarInfoOperationService.checkShahkarInfo(mobile, nationalCode, false);
            log.info("nationalCode({}) and mobile({}) are related together.", nationalCode, mobile);
        }

        WalletTypeEntity walletTypeEntity = walletTypeService.getByNameManaged(walletTypeString);
        if (walletTypeEntity == null) {
            log.error("wallet type with name ({}) not found", walletTypeString);
            throw new InternalServiceException("walletType not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return redisLockService.runAfterLock(nationalCode, this.getClass(), () -> {

            WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());
            if (walletEntity != null) {

                if(!Utility.cleanPhoneNumber(walletEntity.getMobile()).equalsIgnoreCase(Utility.cleanPhoneNumber(mobile))) {
                    log.error("wallet with nationalCode ({}) exist with another mobile number ({})", nationalCode, walletEntity.getMobile());
                    throw new InternalServiceException("wallet exist with another mobileNumber", StatusService.WALLET_EXIST_WITH_ANOTHER_MOBILE, HttpStatus.OK);
                }

                List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletEntity);
                log.info("Wallet already exists with id {}", walletEntity.getId());
                if (walletAccountEntityList.isEmpty()) {
                    log.error("walletAccount with nationalCode ({}) is not create success", nationalCode);
                    throw new InternalServiceException("walletAccount is not create success", StatusService.WALLET_NOT_CREATE_SUCCESS, HttpStatus.OK);
                }
                return helper.fillCreateWalletResponse(walletEntity, walletAccountEntityList, walletAccountService);
            }

            log.info("start create wallet with nationalCode ===> {}", nationalCode);

            walletEntity = new WalletEntity();
            walletEntity.setMobile(mobile);
            walletEntity.setNationalCode(nationalCode);
            walletEntity.setDescription("");
            walletEntity.setOwner(channelEntity);
            walletEntity.setWalletTypeEntity(walletTypeEntity);
            walletEntity.setStatus(WalletStatusEnum.ACTIVE);
            walletEntity.setWalletLevelEntity(walletLevelService.getByLevelManaged(WalletLevelService.BRONZE));
            walletEntity.setCreatedBy(nationalCode);
            walletEntity.setCreatedAt(new Date());
            walletService.save(walletEntity);

            walletAccountService.createAccount(walletAccountCurrencyList, walletEntity, walletAccountTypeList, channelEntity);

            return helper.fillCreateWalletResponse(walletEntity, walletAccountService.findByWallet(walletEntity), walletAccountService);
        }, nationalCode);
    }

    @Override
    public BaseResponse deactivateWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletService.findById(Long.parseLong(walletId));
        walletEntity.setStatus(WalletStatusEnum.DISABLE);
        walletEntity.setUpdatedAt(new Date());
        walletService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public BaseResponse deleteWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletService.findById(Long.parseLong(walletId));
        walletEntity.setNationalCode(walletEntity.getMobile() + "-" + walletEntity.getId());
        walletEntity.setStatus(WalletStatusEnum.DELETED);
        walletEntity.setEndTime(new Date());
        walletService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public BaseResponse activateWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletService.findById(Long.parseLong(walletId));
        walletEntity.setStatus(WalletStatusEnum.ACTIVE);
        walletEntity.setUpdatedAt(new Date());
        walletService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public void getStatement(ChannelEntity channelEntity, Map<String, String> mapParameter, String ip) throws InternalServiceException {

        log.info("start find all GeneralCustomLimitation for username ({}), mapParameter ({})",
                channelEntity.getUsername(), Utility.mapToJsonOrNull(mapParameter));
        if (mapParameter == null) {
            mapParameter = new HashMap<>();
        }
        Pageable pageRequest = getPageableConfig(
                settingGeneralService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        WalletTypeEntity walletTypeEntity = walletTypeService.getByNameManaged(WalletTypeService.NORMAL_USER);
        WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(mapParameter.get("nationalCode"), walletTypeEntity.getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletEntity);
        Optional<WalletAccountEntity> walletAccountEntityOptional = walletAccountEntityList.stream().filter(x->x.getWalletAccountCurrencyEntity().getName().equalsIgnoreCase(WalletAccountCurrencyService.GOLD)).findFirst();
        if(walletAccountEntityOptional.isEmpty()) {
            log.error("wallet for nationalCode ({}) not exist", walletEntity.getNationalCode());
            throw new InternalServiceException("walletAccount not found", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
    }

    @Override
    public CreateWalletResponse get(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException {

        WalletTypeEntity walletTypeEntity = walletTypeService.getByNameManaged(WalletTypeService.NORMAL_USER);
        if (walletTypeEntity == null) {
            log.error("walletType with name ({}) not found", WalletTypeService.NORMAL_USER);
            throw new InternalServiceException("walletType not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());
        if (walletEntity == null) {
            log.error("wallet is not create");
            throw new InternalServiceException("walletAccount is not create success", StatusService.WALLET_NOT_FOUND, HttpStatus.OK);
        }

        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletEntity);
        if (walletAccountEntityList.isEmpty()) {
            log.error("walletAccount is not create success");
            throw new InternalServiceException("walletAccount is not create success", StatusService.WALLET_NOT_CREATE_SUCCESS, HttpStatus.OK);
        }
        return helper.fillCreateWalletResponse(walletEntity, walletAccountEntityList, walletAccountService);
    }

    public Specification<LimitationGeneralCustomEntity> getPredicate(Map<String, String> searchCriteria) throws InternalServiceException{
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
