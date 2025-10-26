package com.melli.wallet.service.impl;

import com.melli.wallet.Constant;
import com.melli.wallet.domain.master.entity.StatPhysicalCashOutEntity;
import com.melli.wallet.domain.master.persistence.StatPhysicalCashOutRepository;
import com.melli.wallet.domain.slave.persistence.ReportPhysicalCashOutRequestRepository;
import com.melli.wallet.service.StatPhysicalCashOutService;
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
public class StatPhysicalCashOutServiceImplementation implements StatPhysicalCashOutService {

    private final StatPhysicalCashOutRepository statPhysicalCashOutRepository;
    private final ReportPhysicalCashOutRequestRepository reportPhysicalCashOutRequestRepository;

    @Override
    public void savePhysicalCashOutStatistics(StatPhysicalCashOutEntity statPhysicalCashOutEntity, String creator) {
        statPhysicalCashOutEntity.setCreatedAt(new Date());
        statPhysicalCashOutEntity.setCreatedBy(creator);
        statPhysicalCashOutRepository.save(statPhysicalCashOutEntity);
    }

    @Override
    public Optional<StatPhysicalCashOutEntity> findLastPhysicalCashOutStatisticsRecord() {
        return statPhysicalCashOutRepository.findFirstByOrderByIdDesc();
    }

    @Override
    public void deletePhysicalCashOutStatisticsByDate(String persianDate) {
        log.info("Starting delete physical cash out statistics by Persian date: {}", persianDate);
        Integer countRowsAffected = statPhysicalCashOutRepository.deleteByPersianCalcDate(persianDate);
        log.info("Finished delete physical cash out statistics by Persian date: {}, deleted rows: {}", persianDate, countRowsAffected);
    }

    @SchedulerLock(
            name = Constant.STAT_PHYSICAL_CASH_OUT_JOB_LOCK,
            lockAtLeastFor = "PT15S", // lock for at least 15 seconds
            lockAtMostFor = "PT3600S" // lock for at most 1 hour
    )
    @Override
    public void generatePhysicalCashOutStatistics(Date fromDate, Date toDate, String creator) {
        log.info("Starting physical cash out statistics generation from date: {} to date: {}", fromDate, toDate);

        Optional<StatPhysicalCashOutEntity> lastPhysicalCashOutRecord = findLastPhysicalCashOutStatisticsRecord();

        if (fromDate == null && lastPhysicalCashOutRecord.isEmpty()) {
            // For first run, start from 30 days ago
            fromDate = DateUtils.getNPreviousDay(new Date(), 30);
        } else if (fromDate == null) {
            // Start from 2 days before the last record to ensure no gaps
            Date lastGeorgianDateRecord = DateUtils.parse(lastPhysicalCashOutRecord.get().getPersianCalcDate(), PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            fromDate = DateUtils.getNPreviousDay(lastGeorgianDateRecord, 2);
        }

        log.info("Processing physical cash out statistics from date: {}", fromDate);

        Date currentDate = fromDate;
        while (!currentDate.after(toDate)) {
            List<ReportPhysicalCashOutRequestRepository.PhysicalCashOutStatPerDay> statPerDayList =
                    reportPhysicalCashOutRequestRepository.findPhysicalCashOutAggregationPerDay(currentDate);

            log.info("Found {} physical cash out statistics records for date: {}", statPerDayList.size(), currentDate);

            if (statPerDayList.isEmpty()) {
                currentDate = DateUtils.getNextDay(currentDate);
                continue;
            }

            // Delete existing records for this date to avoid duplicates
            String persianDate = formatToPersianDate(currentDate);
            deletePhysicalCashOutStatisticsByDate(persianDate);

            // Save new statistics
            for (ReportPhysicalCashOutRequestRepository.PhysicalCashOutStatPerDay row : statPerDayList) {
                String persianCalcDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE), DEFAULT_DATE_FORMAT, false);
                Date georgianDate = DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE);

                StatPhysicalCashOutEntity statPhysicalCashOutEntity = new StatPhysicalCashOutEntity(
                        row.getChannelId(),
                        row.getCurrencyId(),
                        String.valueOf(row.getResult()),
                        row.getCount(),
                        java.math.BigDecimal.valueOf(row.getAmount()),
                        persianCalcDate,
                        georgianDate
                );

                savePhysicalCashOutStatistics(statPhysicalCashOutEntity, creator);
            }

            currentDate = DateUtils.getNextDay(currentDate);
        }

        log.info("Finished physical cash out statistics generation");
    }

    private String formatToPersianDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy/MM/dd").format(date);
    }
}
