package com.melli.wallet.service.impl;

import com.melli.wallet.Constant;
import com.melli.wallet.domain.master.entity.StatBuyEntity;
import com.melli.wallet.domain.master.persistence.StatBuyRepository;
import com.melli.wallet.domain.slave.persistence.ReportPurchaseRequestRepository;
import com.melli.wallet.service.StatBuyService;
import com.melli.wallet.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.melli.wallet.util.date.DateUtils.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class StatBuyServiceImplementation implements StatBuyService {

    private final StatBuyRepository statBuyRepository;
    private final ReportPurchaseRequestRepository reportPurchaseRequestRepository;

    @Override
    public void saveBuyStatistics(StatBuyEntity statBuyEntity, String creator) {
        statBuyEntity.setCreatedAt(new Date());
        statBuyEntity.setCreatedBy(creator);
        statBuyRepository.save(statBuyEntity);
    }

    @Override
    public Optional<StatBuyEntity> findLastBuyStatisticsRecord() {
        return statBuyRepository.findFirstByOrderByIdDesc();
    }

    @Override
    public void deleteBuyStatisticsByDate(String persianDate) {
        log.info("Starting delete buy statistics by Persian date: {}", persianDate);
        Integer countRowsAffected = statBuyRepository.deleteByPersianCalcDate(persianDate);
        log.info("Finished delete buy statistics by Persian date: {}, deleted rows: {}", persianDate, countRowsAffected);
    }

    @SchedulerLock(
            name = Constant.STAT_BUY_JOB_LOCK,
            lockAtLeastFor = "PT15S", // lock for at least 15 seconds
            lockAtMostFor = "PT3600S" // lock for at most 1 hour
    )
    @Override
    public void generateBuyStatistics(Date fromDate, Date toDate, String creator) {
        log.info("Starting buy statistics generation from date: {} to date: {}", fromDate, toDate);

        Optional<StatBuyEntity> lastBuyRecord = findLastBuyStatisticsRecord();

        if (fromDate == null && lastBuyRecord.isEmpty()) {
            // For first run, start from 30 days ago
            fromDate = DateUtils.getNPreviousDay(new Date(), 30);
        } else if (fromDate == null) {
            // Start from 2 days before the last record to ensure no gaps
            Date lastGeorgianDateRecord = DateUtils.parse(lastBuyRecord.get().getPersianCalcDate(), PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            fromDate = DateUtils.getNPreviousDay(lastGeorgianDateRecord, 2);
        }

        log.info("Processing buy statistics from date: {}", fromDate);

        Date currentDate = fromDate;
        while (!currentDate.after(toDate)) {
            List<ReportPurchaseRequestRepository.PurchaseStatPerDay> statPerDayList =
                    reportPurchaseRequestRepository.findPurchaseAggregationPerDay(currentDate);

            log.info("Found {} buy statistics records for date: {}", statPerDayList.size(), currentDate);

            if (statPerDayList.isEmpty()) {
                currentDate = DateUtils.getNextDay(currentDate);
                continue;
            }

            // Delete existing records for this date to avoid duplicates
            String persianDate = formatToPersianDate(currentDate);
            deleteBuyStatisticsByDate(persianDate);

            // Save new statistics
            for (ReportPurchaseRequestRepository.PurchaseStatPerDay row : statPerDayList) {
                String persianCalcDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE), DEFAULT_DATE_FORMAT, false);
                Date georgianDate = DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE);

                StatBuyEntity statBuyEntity = new StatBuyEntity(
                        row.getChannelId(),
                        row.getCurrencyId(),
                        row.getMerchantId(),
                        String.valueOf(row.getResult()),
                        row.getCount(),
                        java.math.BigDecimal.valueOf(row.getAmount()),
                        row.getPrice(),
                        persianCalcDate,
                        georgianDate
                );

                saveBuyStatistics(statBuyEntity, creator);
            }

            currentDate = DateUtils.getNextDay(currentDate);
        }

        log.info("Finished buy statistics generation");
    }


    private String formatToPersianDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy/MM/dd").format(date);
    }
}
