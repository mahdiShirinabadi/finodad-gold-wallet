package com.melli.wallet.service.impl;

import com.melli.wallet.Constant;
import com.melli.wallet.domain.master.entity.StatSellEntity;
import com.melli.wallet.domain.master.persistence.StatSellRepository;
import com.melli.wallet.domain.slave.persistence.ReportSellCollateralRequestRepository;
import com.melli.wallet.service.StatSellService;
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
public class StatSellServiceImplementation implements StatSellService {

    private final StatSellRepository statSellRepository;
    private final ReportSellCollateralRequestRepository reportSellCollateralRequestRepository;

    @Override
    public void saveSellStatistics(StatSellEntity statSellEntity, String creator) {
        statSellEntity.setCreatedAt(new Date());
        statSellEntity.setCreatedBy(creator);
        statSellRepository.save(statSellEntity);
    }

    @Override
    public Optional<StatSellEntity> findLastSellStatisticsRecord() {
        return statSellRepository.findFirstByOrderByIdDesc();
    }

    @Override
    public void deleteSellStatisticsByDate(String persianDate) {
        log.info("Starting delete sell statistics by Persian date: {}", persianDate);
        Integer countRowsAffected = statSellRepository.deleteByPersianCalcDate(persianDate);
        log.info("Finished delete sell statistics by Persian date: {}, deleted rows: {}", persianDate, countRowsAffected);
    }

    @SchedulerLock(
            name = Constant.STAT_SELL_JOB_LOCK,
            lockAtLeastFor = "PT15S", // lock for at least 15 seconds
            lockAtMostFor = "PT3600S" // lock for at most 1 hour
    )
    @Override
    public void generateSellStatistics(Date fromDate, Date toDate, String creator) {
        log.info("Starting sell statistics generation from date: {} to date: {}", fromDate, toDate);

        Optional<StatSellEntity> lastSellRecord = findLastSellStatisticsRecord();

        if (fromDate == null && lastSellRecord.isEmpty()) {
            // For first run, start from 30 days ago
            fromDate = DateUtils.getNPreviousDay(new Date(), 30);
        } else if (fromDate == null) {
            // Start from 2 days before the last record to ensure no gaps
            Date lastGeorgianDateRecord = DateUtils.parse(lastSellRecord.get().getPersianCalcDate(), PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            fromDate = DateUtils.getNPreviousDay(lastGeorgianDateRecord, 2);
        }

        log.info("Processing sell statistics from date: {}", fromDate);

        Date currentDate = fromDate;
        while (!currentDate.after(toDate)) {
            List<ReportSellCollateralRequestRepository.SellStatPerDay> statPerDayList =
                    reportSellCollateralRequestRepository.findSellAggregationPerDay(currentDate);

            log.info("Found {} sell statistics records for date: {}", statPerDayList.size(), currentDate);

            if (statPerDayList.isEmpty()) {
                currentDate = DateUtils.getNextDay(currentDate);
                continue;
            }

            // Delete existing records for this date to avoid duplicates
            String persianDate = formatToPersianDate(currentDate);
            deleteSellStatisticsByDate(persianDate);

            // Save new statistics
            for (ReportSellCollateralRequestRepository.SellStatPerDay row : statPerDayList) {
                String persianCalcDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE), DEFAULT_DATE_FORMAT, false);
                Date georgianDate = DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE);

                StatSellEntity statSellEntity = new StatSellEntity(
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

                saveSellStatistics(statSellEntity, creator);
            }

            currentDate = DateUtils.getNextDay(currentDate);
        }

        log.info("Finished sell statistics generation");
    }

    private String formatToPersianDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy/MM/dd").format(date);
    }
}
