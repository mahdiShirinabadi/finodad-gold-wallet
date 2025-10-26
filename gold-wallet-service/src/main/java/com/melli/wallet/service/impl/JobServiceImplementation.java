package com.melli.wallet.service.impl;

import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Date;

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

        statPhysicalCashOutService.generatePhysicalCashOutStatistics(fromDateParsed, toDateParsed, creator);
    }
}
