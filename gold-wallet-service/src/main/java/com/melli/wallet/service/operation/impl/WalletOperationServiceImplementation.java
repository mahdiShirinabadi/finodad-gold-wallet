package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.ShahkarInfoOperationService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.utils.RedisLockService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    private final WalletRepositoryService walletRepositoryService;
    private final Helper helper;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletTypeRepositoryService walletTypeRepositoryService;
    private final WalletLevelRepositoryService walletLevelRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;

    @Override
    @LogExecutionTime("Creating wallet for user")
    public CreateWalletResponse createWallet(ChannelEntity channelEntity, String mobile, String nationalCode, String walletTypeString, List<String> walletAccountCurrencyList, List<String> walletAccountTypeList) throws InternalServiceException {
        if (channelEntity.getCheckShahkar() == ChannelRepositoryService.TRUE || StringUtils.hasText(nationalCode)) {
            log.info("Start checking if nationalCode({}) and mobile({}) are related from shahkar or not ...", nationalCode, mobile);
            shahkarInfoOperationService.checkShahkarInfo(mobile, nationalCode, false);
            log.info("nationalCode({}) and mobile({}) are related together.", nationalCode, mobile);
        }

        WalletTypeEntity walletTypeEntity = walletTypeRepositoryService.getByNameManaged(walletTypeString);
        if (walletTypeEntity == null) {
            log.error("wallet type with name ({}) not found", walletTypeString);
            throw new InternalServiceException("walletType not found", StatusRepositoryService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return redisLockService.runAfterLock(nationalCode, this.getClass(), () -> {

            WalletEntity walletEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());
            if (walletEntity != null) {

                if (!Utility.cleanPhoneNumber(walletEntity.getMobile()).equalsIgnoreCase(Utility.cleanPhoneNumber(mobile))) {
                    log.error("wallet with nationalCode ({}) exist with another mobile number ({})", nationalCode, walletEntity.getMobile());
                    throw new InternalServiceException("wallet exist with another mobileNumber", StatusRepositoryService.WALLET_EXIST_WITH_ANOTHER_MOBILE, HttpStatus.OK);
                }

                List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletEntity);
                log.info("Wallet already exists with id {}", walletEntity.getId());
                if (walletAccountEntityList.isEmpty()) {
                    log.error("walletAccount with nationalCode ({}) is not create success", nationalCode);
                    throw new InternalServiceException("walletAccount is not create success", StatusRepositoryService.WALLET_NOT_CREATE_SUCCESS, HttpStatus.OK);
                }
                return helper.fillCreateWalletResponse(walletEntity, walletAccountEntityList, walletAccountRepositoryService);
            }

            log.info("start create wallet with nationalCode ===> {}", nationalCode);

            walletEntity = new WalletEntity();
            walletEntity.setMobile(mobile);
            walletEntity.setNationalCode(nationalCode);
            walletEntity.setDescription("");
            walletEntity.setOwner(channelEntity);
            walletEntity.setWalletTypeEntity(walletTypeEntity);
            walletEntity.setStatus(WalletStatusEnum.ACTIVE);
            walletEntity.setWalletLevelEntity(walletLevelRepositoryService.getByLevelManaged(WalletLevelRepositoryService.BRONZE));
            walletEntity.setCreatedBy(nationalCode);
            walletEntity.setCreatedAt(new Date());
            walletRepositoryService.save(walletEntity);

            walletAccountRepositoryService.createAccount(walletAccountCurrencyList, walletEntity, walletAccountTypeList, channelEntity);

            return helper.fillCreateWalletResponse(walletEntity, walletAccountRepositoryService.findByWallet(walletEntity), walletAccountRepositoryService);
        }, nationalCode);
    }

    @Override
    public BaseResponse deactivateWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletRepositoryService.findById(Long.parseLong(walletId));
        walletEntity.setStatus(WalletStatusEnum.DISABLE);
        walletEntity.setUpdatedAt(new Date());
        walletRepositoryService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public BaseResponse deleteWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletRepositoryService.findById(Long.parseLong(walletId));
        walletEntity.setNationalCode(walletEntity.getMobile() + "-" + walletEntity.getId());
        walletEntity.setStatus(WalletStatusEnum.DELETED);
        walletEntity.setEndTime(new Date());
        walletRepositoryService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public BaseResponse activateWallet(ChannelEntity channelEntity, String walletId, String ip) throws InternalServiceException {
        WalletEntity walletEntity = walletRepositoryService.findById(Long.parseLong(walletId));
        walletEntity.setStatus(WalletStatusEnum.ACTIVE);
        walletEntity.setUpdatedAt(new Date());
        walletRepositoryService.save(walletEntity);
        return helper.fillBaseResponse(true, null);
    }

    @Override
    public CreateWalletResponse get(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException {

        WalletTypeEntity walletTypeEntity = walletTypeRepositoryService.getByNameManaged(WalletTypeRepositoryService.NORMAL_USER);
        if (walletTypeEntity == null) {
            log.error("walletType with name ({}) not found", WalletTypeRepositoryService.NORMAL_USER);
            throw new InternalServiceException("walletType not found", StatusRepositoryService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        WalletEntity walletEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());
        if (walletEntity == null) {
            log.error("wallet is not create");
            throw new InternalServiceException("walletAccount is not create success", StatusRepositoryService.WALLET_NOT_FOUND, HttpStatus.OK);
        }

        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletEntity);
        walletAccountEntityList = walletAccountEntityList.stream().filter(x-> (x.getWalletAccountTypeEntity().getDisplay() == Boolean.TRUE)).toList();
        return helper.fillCreateWalletResponse(walletEntity, walletAccountEntityList, walletAccountRepositoryService);
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

    public WalletEntity findUserWallet(String nationalCode) throws InternalServiceException {
        WalletTypeEntity walletType = walletTypeRepositoryService.getByNameManaged(WalletTypeRepositoryService.NORMAL_USER);
        if (walletType == null) {
            log.error("Wallet type {} not found", WalletTypeRepositoryService.NORMAL_USER);
            throw new InternalServiceException("Wallet type not found", StatusRepositoryService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        WalletEntity wallet = walletRepositoryService.findByNationalCodeAndWalletTypeId(nationalCode, walletType.getId());
        if (wallet == null) {
            log.error("National code {} doesn't exist", nationalCode);
            throw new InternalServiceException("National code doesn't exist", StatusRepositoryService.NATIONAL_CODE_NOT_FOUND, HttpStatus.OK);
        }
        return wallet;
    }
}
