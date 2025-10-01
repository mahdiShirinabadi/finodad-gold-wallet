package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.*;
import com.melli.wallet.domain.enumaration.CollateralStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.CreateCollateralRequestRepository;
import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.CollateralHelperService;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class Name: CollateralValidationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 10/1/2025
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CollateralHelperServiceImplementation implements CollateralHelperService {

    private final CreateCollateralRequestRepository createCollateralRequestRepository;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public void checkReleaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, ReleaseCollateralObjectDTO objectDTO) throws InternalServiceException {
        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("checkReleaseCollateral owner collateral for code ({}) is ({}) and not same with channel caller ({})", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("checkReleaseCollateral collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("checkReleaseCollateral collateral with code ({}) release before!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("checkReleaseCollateral collateral  release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getFinalBlockQuantity().compareTo(objectDTO.getQuantity()) != 0) {
            log.error("checkReleaseCollateral quantity collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("checkReleaseCollateral collateral quantity not same", StatusRepositoryService.COLLATERAL_QUANTITY_NOT_SAME, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("checkReleaseCollateral nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("checkReleaseCollateral collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    @Override
    public void checkIncreaseCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, IncreaseCollateralObjectDTO objectDTO) throws InternalServiceException {
        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({})", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("checkIncreaseCollateral collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("collateral with code ({}) release before!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("checkIncreaseCollateral collateral release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("checkIncreaseCollateral collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    @Override
    public void checkSeizeCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SeizeCollateralObjectDTO objectDTO) throws InternalServiceException {
        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({}) in seize", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("collateral with code ({}) release before in seize!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral release before", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.SEIZE.toString())) {
            log.error("collateral with code ({}) release before in seize!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral seize before", StatusRepositoryService.COLLATERAL_SEIZE_BEFORE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }
    }

    @Override
    public void checkSellCollateral(CreateCollateralRequestEntity createCollateralRequestEntity, SellCollateralObjectDTO objectDTO) throws InternalServiceException {

        if (createCollateralRequestEntity.getChannel().getId() != objectDTO.getChannelEntity().getId()) {
            log.error("owner collateral for code ({}) is ({}) and not same with channel caller ({}) in sell", createCollateralRequestEntity.getChannel().getUsername(), objectDTO.getChannelEntity().getUsername(), objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral code not found", StatusRepositoryService.OWNER_COLLATERAL_CODE_SAME, HttpStatus.OK);
        }

        if (createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.RELEASE.toString())) {
            log.error("collateral with code ({}) release before in sell!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral channel not same", StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getCollateralStatusEnum().toString().equalsIgnoreCase(CollateralStatusEnum.SEIZE.toString())) {
            log.error("collateral with code ({}) must be SEIZE!!!", objectDTO.getCollateralCode());
            throw new InternalServiceException("collateral step not valid", StatusRepositoryService.COLLATERAL_STEP_MUST_BE_SEIZE, HttpStatus.OK);
        }

        if (!createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode().equalsIgnoreCase(objectDTO.getNationalCode())) {
            log.error("nationalCode collateral with code ({}) is ({}) and not same with collateral sell ({})", objectDTO.getCollateralCode(), objectDTO.getNationalCode(), createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
            throw new InternalServiceException("collateral nationalCode not same", StatusRepositoryService.COLLATERAL_NATIONAL_CODE_NOT_SAME, HttpStatus.OK);
        }

        if (objectDTO.getQuantity().compareTo(createCollateralRequestEntity.getQuantity()) > 0) {
            log.error("quantity for sell ({}) is bigger than first quantity in collateral ({})", objectDTO.getQuantity(), createCollateralRequestEntity.getQuantity());
            throw new InternalServiceException("quantity is bigger than input quantity", StatusRepositoryService.COLLATERAL_QUANTITY_IS_BIGGER_THAN_BLOCK_QUANTITY, HttpStatus.OK);
        }
    }

    @Override
    public CreateCollateralRequestEntity createCreateCollateralRequestEntity(CreateCollateralObjectDTO objectDTO, WalletAccountEntity walletAccountEntity, RrnEntity rrnEntity,
                                                                              RequestTypeEntity requestTypeEntity, CollateralEntity collateralEntity) {
        CreateCollateralRequestEntity requestEntity = new CreateCollateralRequestEntity();
        requestEntity.setCommission(objectDTO.getCommission());
        requestEntity.setQuantity(objectDTO.getQuantity());
        requestEntity.setFinalBlockQuantity(objectDTO.getQuantity());
        requestEntity.setWalletAccountEntity(walletAccountEntity);
        requestEntity.setRrnEntity(rrnEntity);
        requestEntity.setChannel(objectDTO.getChannelEntity());
        requestEntity.setResult(StatusRepositoryService.CREATE);
        requestEntity.setChannelIp(objectDTO.getIp());
        requestEntity.setRequestTypeEntity(requestTypeEntity);
        requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
        requestEntity.setCreatedAt(new Date());
        requestEntity.setCollateralStatusEnum(CollateralStatusEnum.CREATE);
        requestEntity.setCode(generateCode());
        requestEntity.setAdditionalData(objectDTO.getDescription());
        requestEntity.setCollateralEntity(collateralEntity);
        return requestEntity;
    }

    public SellCollateralRequestEntity createSellCollateralRequestEntity(SellCollateralObjectDTO objectDTO, MerchantRepositoryService merchantRepositoryService, RequestTypeEntity requestTypeEntity, CreateCollateralRequestEntity createCollateralRequestEntity,
                                                                         WalletAccountEntity collateralWalletAccountEntity) {
        SellCollateralRequestEntity requestEntity = new SellCollateralRequestEntity();
        requestEntity.setMerchantEntity(merchantRepositoryService.findById(Integer.parseInt(objectDTO.getMerchantId())));
        requestEntity.setCollateralWalletAccountEntity(collateralWalletAccountEntity);
        requestEntity.setPrice(Long.parseLong(objectDTO.getPrice()));
        requestEntity.setCommission(objectDTO.getCommission());
        requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
        requestEntity.setChannel(objectDTO.getChannelEntity());
        requestEntity.setResult(StatusRepositoryService.CREATE);
        requestEntity.setChannelIp(objectDTO.getIp());
        requestEntity.setRequestTypeEntity(requestTypeEntity);
        requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
        requestEntity.setCreatedAt(new Date());
        requestEntity.setAdditionalData(objectDTO.getDescription());
        requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
        requestEntity.setCashOutRequestEntity(null);
        requestEntity.setQuantity(objectDTO.getQuantity());
        requestEntity.setIban(createCollateralRequestEntity.getCollateralEntity().getIban());
        return requestEntity;
    }

    public ReleaseCollateralRequestEntity createReleaseCollateralRequestEntity(ReleaseCollateralObjectDTO objectDTO, RequestTypeEntity requestTypeEntity, CreateCollateralRequestEntity createCollateralRequestEntity,
                                                                         WalletAccountEntity walletAccountEntity) {
        ReleaseCollateralRequestEntity requestEntity = new ReleaseCollateralRequestEntity();
        requestEntity.setCommission(objectDTO.getCommission());
        requestEntity.setQuantity(objectDTO.getQuantity());
        requestEntity.setWalletAccountEntity(walletAccountEntity);
        requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
        requestEntity.setChannel(objectDTO.getChannelEntity());
        requestEntity.setResult(StatusRepositoryService.CREATE);
        requestEntity.setChannelIp(objectDTO.getIp());
        requestEntity.setRequestTypeEntity(requestTypeEntity);
        requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
        requestEntity.setCreatedAt(new Date());
        requestEntity.setAdditionalData(objectDTO.getDescription());
        requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
        return requestEntity;
    }

    @Override
    public IncreaseCollateralRequestEntity createIncreaseCollateralRequestEntity(IncreaseCollateralObjectDTO objectDTO, WalletAccountEntity walletAccountEntity, CreateCollateralRequestEntity createCollateralRequestEntity, RrnEntity rrnEntity, RequestTypeEntity requestTypeEntity) {
        IncreaseCollateralRequestEntity requestEntity = new IncreaseCollateralRequestEntity();
        requestEntity.setCommission(objectDTO.getCommission());
        requestEntity.setQuantity(objectDTO.getQuantity());
        requestEntity.setWalletAccountEntity(walletAccountEntity);
        requestEntity.setRrnEntity(rrnEntity);
        requestEntity.setChannel(objectDTO.getChannelEntity());
        requestEntity.setResult(StatusRepositoryService.CREATE);
        requestEntity.setChannelIp(objectDTO.getIp());
        requestEntity.setRequestTypeEntity(requestTypeEntity);
        requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
        requestEntity.setCreatedAt(new Date());
        requestEntity.setAdditionalData(objectDTO.getDescription());
        requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
        return requestEntity;
    }

    @Override
    public SeizeCollateralRequestEntity createSeizeCollateralRequestEntity(SeizeCollateralObjectDTO objectDTO, WalletAccountEntity walletAccountEntity, CreateCollateralRequestEntity createCollateralRequestEntity, RequestTypeEntity requestTypeEntity) {
        SeizeCollateralRequestEntity requestEntity = new SeizeCollateralRequestEntity();
        requestEntity.setWalletAccountEntity(walletAccountEntity);
        requestEntity.setRrnEntity(createCollateralRequestEntity.getRrnEntity());
        requestEntity.setChannel(objectDTO.getChannelEntity());
        requestEntity.setResult(StatusRepositoryService.CREATE);
        requestEntity.setChannelIp(objectDTO.getIp());
        requestEntity.setRequestTypeEntity(requestTypeEntity);
        requestEntity.setCreatedBy(objectDTO.getChannelEntity().getUsername());
        requestEntity.setCreatedAt(new Date());
        requestEntity.setAdditionalData(objectDTO.getDescription());
        requestEntity.setCreateCollateralRequestEntity(createCollateralRequestEntity);
        return requestEntity;
    }

    @Override
    public List<Predicate> buildReportTransactionPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ReportTransactionEntity> root,
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

    @Override
    public TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, RequestTypeEntity requestTypeEntity,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestTypeEntity.getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }

    private String generateCode() {
        while (true) {
            String scratchCode = generateRandomString();
            Long countRecord = createCollateralRequestRepository.countByCode(scratchCode);
            if (countRecord == 0) {
                return scratchCode;
            }
        }
    }

    private String generateRandomString() {
        String saltChars = "ABCDEFGHJKMNLPQRSTUVWXYZ23456789$#@";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < 15) { // length of the random string.
            int index = random.nextInt(saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    public List<Predicate> buildCreateCollateralPredicatesFromCriteria(Map<String, String> searchCriteria, Root<CreateCollateralRequestEntity> root,
                                                                        CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String fromTime = searchCriteria.get("fromTime");
        String toTime = searchCriteria.get("toTime");
        String nationalCode = searchCriteria.get("nationalCode");
        String collateralId = searchCriteria.get("collateralId");

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(searchCriteria.get("id"))));
        }

        if (StringUtils.hasText(nationalCode)) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountEntity").get("walletEntity").get("nationalCode"), nationalCode));
        }


        if (StringUtils.hasText(collateralId)) {
            predicates.add(criteriaBuilder.equal(root.get("collateralEntity").get("id"), collateralId));
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
}
