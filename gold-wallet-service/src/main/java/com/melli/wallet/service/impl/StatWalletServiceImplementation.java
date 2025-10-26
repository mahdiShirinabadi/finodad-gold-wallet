package com.melli.wallet.service.impl;

import com.melli.wallet.Constant;
import com.melli.wallet.domain.master.entity.StatWalletEntity;
import com.melli.wallet.domain.master.persistence.StatWalletRepository;
import com.melli.wallet.domain.slave.persistence.ReportWalletAccountRepository;
import com.melli.wallet.service.StatWalletService;
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
public class StatWalletServiceImplementation implements StatWalletService {

    private final StatWalletRepository statWalletRepository;
    private final ReportWalletAccountRepository reportWalletAccountRepository;

    @Override
    public void saveWalletStatistics(StatWalletEntity statWalletEntity, String creator) {
        statWalletEntity.setCreatedAt(new Date());
        statWalletEntity.setCreatedBy(creator);
        statWalletRepository.save(statWalletEntity);
    }

    @Override
    public Optional<StatWalletEntity> findLastWalletStatisticsRecord() {
        return statWalletRepository.findFirstByOrderByIdDesc();
    }

    @Override
    public void deleteWalletStatisticsByDate(String persianDate) {
        log.info("Starting delete wallet statistics by Persian date: {}", persianDate);
        Integer countRowsAffected = statWalletRepository.deleteByPersianCalcDate(persianDate);
        log.info("Finished delete wallet statistics by Persian date: {}, deleted rows: {}", persianDate, countRowsAffected);
    }

    @SchedulerLock(
            name = Constant.STAT_WALLET_JOB_LOCK,
            lockAtLeastFor = "PT15S", // lock for at least 15 seconds
            lockAtMostFor = "PT3600S" // lock for at most 1 hour
    )
    @Override
    public void generateWalletStatistics(Date fromDate, Date toDate, String creator) {
        log.info("Starting wallet statistics generation from date: {} to date: {}", fromDate, toDate);

        Optional<StatWalletEntity> lastWalletRecord = findLastWalletStatisticsRecord();

        if (fromDate == null && lastWalletRecord.isEmpty()) {
            // For first run, start from 30 days ago
            fromDate = DateUtils.getNPreviousDay(new Date(), 30);
        } else if (fromDate == null) {
            // Start from 2 days before the last record to ensure no gaps
            Date lastGeorgianDateRecord = DateUtils.parse(lastWalletRecord.get().getPersianCalcDate(), PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            fromDate = DateUtils.getNPreviousDay(lastGeorgianDateRecord, 2);
        }

        log.info("Processing wallet statistics from date: {}", fromDate);

        Date currentDate = fromDate;
        while (!currentDate.after(toDate)) {
            List<ReportWalletAccountRepository.WalletStatPerDay> statPerDayList =
                    reportWalletAccountRepository.findWalletAggregationPerDay(currentDate);

            log.info("Found {} wallet statistics records for date: {}", statPerDayList.size(), currentDate);

            if (statPerDayList.isEmpty()) {
                currentDate = DateUtils.getNextDay(currentDate);
                continue;
            }

            // Delete existing records for this date to avoid duplicates
            String persianDate = formatToPersianDate(currentDate);
            deleteWalletStatisticsByDate(persianDate);

            // Save new statistics
            for (ReportWalletAccountRepository.WalletStatPerDay row : statPerDayList) {
                String persianCalcDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE), DEFAULT_DATE_FORMAT, false);
                Date georgianDate = DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE);

                StatWalletEntity statWalletEntity = new StatWalletEntity(
                        row.getChannelId(),
                        row.getCount(),
                        persianCalcDate,
                        georgianDate
                );

                saveWalletStatistics(statWalletEntity, creator);
            }

            currentDate = DateUtils.getNextDay(currentDate);
        }

        log.info("Finished wallet statistics generation");
    }

    private String formatToPersianDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy/MM/dd").format(date);
    }
}
