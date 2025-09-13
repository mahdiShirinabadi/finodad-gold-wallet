package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.persistence.ChannelRepository;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.domain.slave.persistence.ReportTransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.ChannelOperationService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletAccountRepositoryService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Class Name: ChannelOperationServiceImpl
 * Author: Mahdi Shirinabadi
 * Date: 9/13/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class ChannelOperationServiceImplementation implements ChannelOperationService {

    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final Helper helper;
    private final ReportTransactionRepository reportTransactionRepository;
    private final ChannelRepository channelRepository;

    @Override
    public WalletBalanceResponse getBalance(ChannelEntity channelEntity) throws InternalServiceException {
        log.info("start get balance for channel ({})", channelEntity.getUsername());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(channelEntity.getWalletEntity());
        return helper.fillWalletBalanceResponse(walletAccountEntityList, walletAccountRepositoryService);
    }


    @Override
    public ReportTransactionResponse report(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException{
        Pageable pageRequest = helper.getPageableConfig(settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        String channelId = mapParameter.get("channelId");
        Optional<ChannelEntity> channelEntityReport = channelRepository.findById(Long.parseLong(channelId));
        if(channelEntityReport.isEmpty()) {
            log.error("channel id {} not found", channelId);
            throw new InternalServiceException("channel Id not found", StatusRepositoryService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        }
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(channelEntityReport.get().getWalletEntity(), pageRequest);
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
}
