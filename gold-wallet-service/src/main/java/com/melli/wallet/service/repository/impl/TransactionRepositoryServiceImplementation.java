package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.TransactionRepository;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.transaction.StatementResponse;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.domain.slave.persistence.ReportTransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;


/**
 * Class Name: TransactionServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class TransactionRepositoryServiceImplementation implements TransactionRepositoryService {

    private final TransactionRepository transactionRepository;
    private final ReportTransactionRepository reportTransactionRepository;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final Helper helper;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final WalletTypeRepositoryService walletTypeRepositoryService;
    private final WalletRepositoryService walletRepositoryService;

    @Override
    public StatementResponse lastTransaction(ChannelEntity channelEntity, String nationalCode, String walletAccountNumber, int limit) throws InternalServiceException {
        WalletTypeEntity walletTypeEntity = walletTypeRepositoryService.getByNameManaged(WalletTypeRepositoryService.NORMAL_USER);
        WalletEntity walletEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletEntity);
        Optional<WalletAccountEntity> walletAccountEntityOptional = walletAccountEntityList.stream().filter(x -> x.getAccountNumber().equalsIgnoreCase(walletAccountNumber)).findFirst();
        if (walletAccountEntityOptional.isEmpty()) {
            log.error("wallet for nationalCode ({}) not exist", walletEntity.getNationalCode());
            throw new InternalServiceException("walletAccount not found", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
        Page<ReportTransactionEntity> reportTransactionEntityPage = reportTransactionRepository.findByWalletAccountEntityIdOrderByIdDesc(walletAccountEntityOptional.get().getId(), PageRequest.of(0, limit));
        return helper.fillStatementResponse(nationalCode, reportTransactionEntityPage.getContent());
    }

    @Override
    public ReportTransactionResponse reportTransaction(ChannelEntity channelEntity, Map<String, String> mapParameter) {

        Pageable pageRequest = helper.getPageableConfig(settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
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
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertDeposit(TransactionEntity transaction) throws InternalServiceException {
        log.info("start deposit amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
        BalanceDTO walletBalance = walletAccountRepositoryService.getBalance(transaction.getWalletAccountEntity().getId());
        transaction.setType(TransactionEntity.DEPOSIT);
        transaction.setCreatedAt(new Date());
        transaction.setCreatedBy("System");
        transaction.setRealBalance(walletBalance.getRealBalance());
        transaction.setAvailableBalance(walletBalance.getAvailableBalance());
        transaction.setWalletAccountEntity(walletAccountRepositoryService.findById(transaction.getWalletAccountEntity().getId()));
        walletAccountRepositoryService.increaseBalance(walletAccountRepositoryService.findById(transaction.getWalletAccountEntity().getId()).getId(), transaction.getAmount());
        transactionRepository.save(transaction);
        log.info("finish deposit amount ( {} ) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertWithdraw(TransactionEntity transaction) throws InternalServiceException {

        log.info("start withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
        // Get current balance
        BalanceDTO walletBalance = walletAccountRepositoryService.getBalance(transaction.getWalletAccountEntity().getId());

        // Check for sufficient balance
        if (walletBalance.getAvailableBalance().subtract(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            log.error("Balance of wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), walletBalance, transaction.getAmount());
            throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
        }

        transaction.setRealBalance(walletBalance.getRealBalance());
        transaction.setAvailableBalance(walletBalance.getAvailableBalance());
        transaction.setType(TransactionEntity.WITHDRAW);
        transaction.setCreatedAt(new Date());
        transaction.setCreatedBy("System");
        int result = walletAccountRepositoryService.decreaseBalance(walletAccountRepositoryService.findById(transaction.getWalletAccountEntity().getId()).getId(), transaction.getAmount());
        if (result <= 0) {
            log.error("Balance of wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), walletBalance, transaction.getAmount());
            throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
        }
        transaction.setWalletAccountEntity(walletAccountRepositoryService.findById(transaction.getWalletAccountEntity().getId()));
        transactionRepository.save(transaction);
        log.info("finish withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void transferBlockWithdrawAndTransfer(TransactionEntity transaction, TransactionEntity destTransaction) throws InternalServiceException {

        log.info("start withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
        // Get current balance
        BigDecimal blockedAmount = walletAccountRepositoryService.getBlockAmount(transaction.getWalletAccountEntity().getId());
        BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(transaction.getWalletAccountEntity().getId());

        // Check for sufficient balance
        if (blockedAmount.subtract(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            log.error("Balance of block wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), blockedAmount, transaction.getAmount());
            throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusRepositoryService.BLOCK_AMOUNT_NOT_ENOUGH, HttpStatus.OK);
        }

        transaction.setRealBalance(balanceDTO.getRealBalance());
        transaction.setAvailableBalance(balanceDTO.getAvailableBalance());
        transaction.setType(TransactionEntity.WITHDRAW);
        transaction.setCreatedAt(new Date());
        transaction.setCreatedBy("System");
        int result = walletAccountRepositoryService.unblockAndDecreaseAmount(walletAccountRepositoryService.findById(transaction.getWalletAccountEntity().getId()).getId(), transaction.getAmount());
        if (result <= 0) {
            log.error("Balance of wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), balanceDTO.getAvailableBalance(), transaction.getAmount());
            throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
        }
        transaction.setWalletAccountEntity(walletAccountRepositoryService.findById(transaction.getWalletAccountEntity().getId()));
        transactionRepository.save(transaction);
        log.info("finish withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());


        log.info("start deposit amount ({}) from walletAccountId ({})", destTransaction.getAmount(), destTransaction.getWalletAccountEntity().getId());
        BalanceDTO walletBalance = walletAccountRepositoryService.getBalance(destTransaction.getWalletAccountEntity().getId());
        destTransaction.setType(TransactionEntity.DEPOSIT);
        destTransaction.setCreatedAt(new Date());
        destTransaction.setCreatedBy("System");
        destTransaction.setRealBalance(walletBalance.getRealBalance());
        destTransaction.setAvailableBalance(walletBalance.getAvailableBalance());
        destTransaction.setWalletAccountEntity(walletAccountRepositoryService.findById(destTransaction.getWalletAccountEntity().getId()));
        walletAccountRepositoryService.increaseBalance(walletAccountRepositoryService.findById(destTransaction.getWalletAccountEntity().getId()).getId(), destTransaction.getAmount());
        transactionRepository.save(destTransaction);
        log.info("finish deposit amount ( {} ) from walletAccountId ({})", destTransaction.getAmount(), destTransaction.getWalletAccountEntity().getId());

    }
}
