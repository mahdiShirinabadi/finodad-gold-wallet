package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.*;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Class Name: CollateralValidationService
 * Author: Mahdi Shirinabadi
 * Date: 10/1/2025
 */
public interface CollateralHelperService {

    void checkReleaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException;
    void checkIncreaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, IncreaseCollateralObjectDTO objectDTO) throws InternalServiceException;
    void checkSeizeCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SeizeCollateralObjectDTO objectDTO) throws InternalServiceException;
    void checkSellCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SellCollateralObjectDTO objectDTO) throws InternalServiceException;
    CreateCollateralRequestEntity createCreateCollateralRequestEntity(CreateCollateralObjectDTO objectDTO, WalletAccountEntity walletAccountEntity, RrnEntity rrnEntity,
                                                                              RequestTypeEntity requestTypeEntity, CollateralEntity collateralEntity);
    SellCollateralRequestEntity createSellCollateralRequestEntity(SellCollateralObjectDTO objectDTO, MerchantRepositoryService merchantRepositoryService, RequestTypeEntity requestTypeEntity, CreateCollateralRequestEntity createCollateralRequestEntity,
                                                                         WalletAccountEntity collateralWalletAccountEntity);

    ReleaseCollateralRequestEntity createReleaseCollateralRequestEntity(ReleaseCollateralObjectDTO objectDTO, RequestTypeEntity requestTypeEntity, CreateCollateralRequestEntity createCollateralRequestEntity,
                                                                               WalletAccountEntity walletAccountEntity);

    IncreaseCollateralRequestEntity createIncreaseCollateralRequestEntity(IncreaseCollateralObjectDTO objectDTO, WalletAccountEntity walletAccountEntity, CreateCollateralRequestEntity createCollateralRequestEntity, RrnEntity rrnEntity, RequestTypeEntity requestTypeEntity);
    SeizeCollateralRequestEntity createSeizeCollateralRequestEntity(SeizeCollateralObjectDTO objectDTO, WalletAccountEntity walletAccountEntity, CreateCollateralRequestEntity createCollateralRequestEntity, RequestTypeEntity requestTypeEntity);

    TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, RequestTypeEntity requestTypeEntity, RrnEntity rrn);
    List<Predicate> buildReportTransactionPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ReportTransactionEntity> root, CriteriaBuilder criteriaBuilder);

    List<Predicate> buildCreateCollateralPredicatesFromCriteria(Map<String, String> searchCriteria, Root<CreateCollateralRequestEntity> root, CriteriaBuilder criteriaBuilder);
}
