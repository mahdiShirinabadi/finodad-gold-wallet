package com.melli.wallet.service.impl;

import com.melli.wallet.Constant;
import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.SettingGeneralEntity;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.service.operation.SettlementService;
import com.melli.wallet.service.repository.RequestRepositoryService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.ShedlockRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.melli.wallet.util.date.DateUtils.PERSIAN_DATE_FORMAT;

@Service
@Log4j2
@RequiredArgsConstructor
public class JobServiceImplementation implements JobService {

    private final StatBuyService statBuyService;
    private final StatSellService statSellService;
    private final StatWalletService statWalletService;
    private final StatPerson2PersonService statPerson2PersonService;
    private final StatPhysicalCashOutService statPhysicalCashOutService;
    private final SettlementService settlementService;
    private final RequestRepositoryService requestRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final ShedlockRepositoryService shedlockRepositoryService;

    @Override
    public void generateBuyStatistics(String fromDate, String toDate, String creator) throws InternalServiceException {
        Date fromDateParsed = null;
        Date toDateParsed = null;

        if (CustomStringUtils.hasText(fromDate)) {
            if (Integer.parseInt(fromDate.substring(0, 4)) < 1900) {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        if (CustomStringUtils.hasText(toDate)) {
            if (Integer.parseInt(toDate.substring(0, 4)) < 1900) {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        shedlockRepositoryService.checkActiveLock(Constant.STAT_BUY_JOB_LOCK);
        statBuyService.generateBuyStatistics(fromDateParsed, toDateParsed, creator);
    }

    @Override
    public void generateSellStatistics(String fromDate, String toDate, String creator) throws InternalServiceException {
        Date fromDateParsed = null;
        Date toDateParsed = null;

        if (CustomStringUtils.hasText(fromDate)) {
            if (Integer.parseInt(fromDate.substring(0, 4)) < 1900) {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        if (CustomStringUtils.hasText(toDate)) {
            if (Integer.parseInt(toDate.substring(0, 4)) < 1900) {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        shedlockRepositoryService.checkActiveLock(Constant.STAT_SELL_JOB_LOCK);
        statSellService.generateSellStatistics(fromDateParsed, toDateParsed, creator);
    }

    @Override
    public void generateWalletStatistics(String fromDate, String toDate, String creator) throws InternalServiceException {
        Date fromDateParsed = null;
        Date toDateParsed = null;

        if (CustomStringUtils.hasText(fromDate)) {
            if (Integer.parseInt(fromDate.substring(0, 4)) < 1900) {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        if (CustomStringUtils.hasText(toDate)) {
            if (Integer.parseInt(toDate.substring(0, 4)) < 1900) {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        shedlockRepositoryService.checkActiveLock(Constant.STAT_WALLET_JOB_LOCK);
        statWalletService.generateWalletStatistics(fromDateParsed, toDateParsed, creator);
    }

    @Override
    public void generatePerson2PersonStatistics(String fromDate, String toDate, String creator) throws InternalServiceException {
        Date fromDateParsed = null;
        Date toDateParsed = null;

        if (CustomStringUtils.hasText(fromDate)) {
            if (Integer.parseInt(fromDate.substring(0, 4)) < 1900) {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        if (CustomStringUtils.hasText(toDate)) {
            if (Integer.parseInt(toDate.substring(0, 4)) < 1900) {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        shedlockRepositoryService.checkActiveLock(Constant.STAT_PERSON2PERSON_JOB_LOCK);
        statPerson2PersonService.generatePerson2PersonStatistics(fromDateParsed, toDateParsed, creator);
    }

    @Override
    public void generatePhysicalCashOutStatistics(String fromDate, String toDate, String creator) throws InternalServiceException {
        Date fromDateParsed = null;
        Date toDateParsed = null;

        if (CustomStringUtils.hasText(fromDate)) {
            if (Integer.parseInt(fromDate.substring(0, 4)) < 1900) {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                fromDateParsed = DateUtils.parse(fromDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        if (CustomStringUtils.hasText(toDate)) {
            if (Integer.parseInt(toDate.substring(0, 4)) < 1900) {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                toDateParsed = DateUtils.parse(toDate, PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
        }

        shedlockRepositoryService.checkActiveLock(Constant.STAT_PHYSICAL_CASH_OUT_JOB_LOCK);
        statPhysicalCashOutService.generatePhysicalCashOutStatistics(fromDateParsed, toDateParsed, creator);
    }

    @Override
    public void batchSettlement() throws InternalServiceException {

        boolean settlementBatch = Boolean.parseBoolean(settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.SETTLEMENT_BATCH).getValue());

        if (!settlementBatch) {
            log.info("settlementBatch setting is false and stop this method and send to job");
            throw new InternalServiceException("SETTLEMENT_BATCH is not active!!", StatusRepositoryService.SETTLEMENT_BATCH_NOT_ACTIVE, HttpStatus.OK);
        }

        log.info("batchSettlement started");
        shedlockRepositoryService.checkActiveLock(Constant.BATCH_SETTLEMENT_JOB_LOCK);
        int limit = getInquiryLimit();
        List<CashOutRequestEntity> cashOutRequestEntityList = requestRepositoryService.findAllCashOutByStep(SettlementStepEnum.INITIAL, limit);
        settlementService.bachSettlement(cashOutRequestEntityList);
        log.info("terminate batchSettlement started");
    }

    private int getInquiryLimit() {
        SettingGeneralEntity setting = settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.COUNT_LIMIT_JOB_SETTLEMENT);
        return Integer.parseInt(setting.getValue());
    }
}
