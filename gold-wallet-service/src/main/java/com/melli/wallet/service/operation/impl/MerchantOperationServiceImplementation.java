package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.domain.slave.persistence.ReportTransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.MerchantOperationService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Class Name: MerchantOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class MerchantOperationServiceImplementation implements MerchantOperationService {

    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final RedisLockService redisLockService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final Helper helper;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final ReportTransactionRepository reportTransactionRepository;

    @Override
    public WalletBalanceResponse getBalance(ChannelEntity channelEntity, String merchantId) throws InternalServiceException {
        log.info("start get balance for merchantId ({})", merchantId);
        MerchantEntity merchantEntity = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        if (merchantEntity == null) {
            log.error("merchant {} not found", merchantId);
            throw new InternalServiceException("merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(merchantEntity.getWalletEntity());
        return helper.fillWalletBalanceResponse(walletAccountEntityList, walletAccountRepositoryService);
    }

    @Override
    public void updateStatus(ChannelEntity channelEntity, String merchantId, String status) throws InternalServiceException {
        log.info("start update status for merchantId ({}) and status ({}) with channel ({})", merchantId, status, channelEntity.getUsername());
        MerchantEntity merchantEntity = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        if (merchantEntity == null) {
            log.error("merchant {} not found", merchantId);
            throw new InternalServiceException("merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        merchantEntity.setStatus(Integer.parseInt(status));
        merchantEntity.setUpdatedAt(new Date());
        merchantEntity.setUpdatedBy(channelEntity.getUsername());
        merchantRepositoryService.save(merchantEntity);
        merchantRepositoryService.clearAllCache();
        log.info("success update status for merchantId ({}) and status ({})", merchantId, status);
    }

    @Override
    public String increaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException {

        // Validate merchant exists
        MerchantEntity merchant = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        if (merchant == null) {
            log.error("Merchant with ID {} not found", merchantId);
            throw new InternalServiceException("Merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }

        // Find wallet account
        WalletAccountEntity walletAccount = walletAccountRepositoryService.findByAccountNumber(walletAccountNumber);
        if (walletAccount == null) {
            log.error("Wallet account with number {} not found", walletAccountNumber);
            throw new InternalServiceException("Wallet account not found", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        // Check if wallet account belongs to the merchant
        if (walletAccount.getWalletEntity().getId() != merchant.getWalletEntity().getId()) {
            log.error("Wallet account {} does not belong to merchant {}", walletAccountNumber, merchantId);
            throw new InternalServiceException("Wallet account does not belong to merchant", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        return redisLockService.runAfterLock(merchantId, this.getClass(), () -> {
            log.info("start increaseBalance for merchantId ({}) with amount ({})", merchantId, amount);


            // Generate RRN for transaction
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(
                    merchant.getWalletEntity().getNationalCode(),
                    channelEntity,
                    requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_INCREASE_BALANCE),
                    walletAccountNumber,
                    amount
            );

            // Create transaction entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(new BigDecimal(amount));
            transaction.setWalletAccountEntity(walletAccount);
            transaction.setRrnEntity(rrnEntity);
            transaction.setRequestTypeId(requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_INCREASE_BALANCE).getId());
            transaction.setAdditionalData("Manual increase by admin");

            // Create description
            transaction.setDescription("افزایش مانده پذیرنده " + merchant.getName() + " به مبلغ " + amount + " - شناسه تراکنش: " + rrnEntity.getId());

            // Execute deposit transaction
            transactionRepositoryService.insertDeposit(transaction);

            log.info("finish increaseBalance for merchant {} with amount {} and traceId {}", merchantId, amount, rrnEntity.getUuid());
            return rrnEntity.getUuid();
        }, merchantId);
    }

    @Override
    public ReportTransactionResponse report(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException {
        Pageable pageRequest = helper.getPageableConfig(settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        String merchantId = mapParameter.get("merchantId");
        MerchantEntity merchantEntity = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(merchantEntity.getWalletEntity());
        String accountNumbersStr = String.join(",", walletAccountEntityList.stream().map(WalletAccountEntity::getAccountNumber).toList());
        mapParameter.put("walletAccountNumber", accountNumbersStr);
        Specification<ReportTransactionEntity> specification = getReportTransactionEntityPredicate(mapParameter);
        Page<ReportTransactionEntity> reportTransactionEntityPage = reportTransactionRepository.findAll(specification, pageRequest);
        return helper.fillReportStatementResponse(reportTransactionEntityPage);

    }


    private Specification<ReportTransactionEntity> getReportTransactionEntityPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ReportTransactionEntity> root,
                                                        CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String fromTime = searchCriteria.get("fromTime");
        String toTime = searchCriteria.get("toTime");
        String nationalCode = searchCriteria.get("nationalCode");
        String walletAccountNumber = searchCriteria.get("walletAccountNumber");
        String uniqueIdentifier = searchCriteria.get("uniqueIdentifier");

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(searchCriteria.get("id"))));
        }

        if (StringUtils.hasText(nationalCode)) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountEntity").get("walletEntity").get("nationalCode"), nationalCode));
        }

        if (StringUtils.hasText(walletAccountNumber)) {
            List<String> stringList = Arrays.stream(walletAccountNumber.split(",")).toList();
            predicates.add(criteriaBuilder.in(root.get("walletAccountEntity").get("accountNumber")).value(stringList));
        }

        if (StringUtils.hasText(uniqueIdentifier)) {
            predicates.add(criteriaBuilder.equal(root.get("rrnEntity").get("uuid"), uniqueIdentifier));
        }

        if ((StringUtils.hasText(fromTime))) {
            Date sDate;
            if (Integer.parseInt(fromTime.substring(0, 4)) < 1900) {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                sDate = DateUtils.parse(fromTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), sDate));
        }

        if (StringUtils.hasText(toTime)) {
            Date tDate;
            if (Integer.parseInt(toTime.substring(0, 4)) < 1900) {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                tDate = DateUtils.parse(toTime, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), tDate));
        }

        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder,
                          Root<ReportTransactionEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }

    @Override
    public String decreaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException {
        return redisLockService.runAfterLock(merchantId, this.getClass(), () -> {
            log.info("start decreaseBalance for merchantId ({}) with amount ({})", merchantId, amount);

            // Validate merchant exists
            MerchantEntity merchant = merchantRepositoryService.findById(Integer.parseInt(merchantId));
            if (merchant == null) {
                log.error("Merchant with ID {} not found", merchantId);
                throw new InternalServiceException("Merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
            }

            // Find wallet account
            WalletAccountEntity walletAccount = walletAccountRepositoryService.findByAccountNumber(walletAccountNumber);
            if (walletAccount == null) {
                log.error("Wallet account with number {} not found", walletAccountNumber);
                throw new InternalServiceException("Wallet account not found", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
            }

            // Check if wallet account belongs to the merchant
            if (walletAccount.getWalletEntity().getId() != merchant.getWalletEntity().getId()) {
                log.error("Wallet account {} does not belong to merchant {}", walletAccountNumber, merchantId);
                throw new InternalServiceException("Wallet account does not belong to merchant", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
            }

            // Check if merchant has sufficient balance
            BalanceDTO currentBalance = walletAccountRepositoryService.getBalance(walletAccount.getId());
            BigDecimal requestedAmount = new BigDecimal(amount);
            if (currentBalance.getRealBalance().compareTo(requestedAmount) < 0) {
                log.error("Insufficient balance for merchant {}. Current: {}, Requested: {}", merchantId, currentBalance, requestedAmount);
                throw new InternalServiceException("Insufficient balance", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }

            // Generate RRN for transaction
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(
                    merchant.getWalletEntity().getNationalCode(),
                    channelEntity,
                    requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_DECREASE_BALANCE),
                    walletAccountNumber,
                    amount
            );

            // Create transaction entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(requestedAmount);
            transaction.setWalletAccountEntity(walletAccount);
            transaction.setRrnEntity(rrnEntity);
            transaction.setRequestTypeId(requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_DECREASE_BALANCE).getId());
            transaction.setAdditionalData("Manual decrease by admin");

            // Create description
            transaction.setDescription("کاهش مانده پذیرنده " + merchant.getName() + " به مبلغ " + amount + " - شناسه تراکنش: " + rrnEntity.getId());

            // Execute withdrawal transaction
            transactionRepositoryService.insertWithdraw(transaction);

            log.info("finish decreaseBalance for merchant {} with amount {} and traceId {}", merchantId, amount, rrnEntity.getUuid());
            return rrnEntity.getUuid();
        }, merchantId);
    }
}
