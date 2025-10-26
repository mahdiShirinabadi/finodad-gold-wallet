package com.melli.wallet.service.impl;

import com.melli.wallet.Constant;
import com.melli.wallet.domain.master.entity.StatPerson2PersonEntity;
import com.melli.wallet.domain.master.persistence.StatPerson2PersonRepository;
import com.melli.wallet.domain.slave.persistence.ReportPerson2PersonRequestRepository;
import com.melli.wallet.service.StatPerson2PersonService;
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
public class StatPerson2PersonServiceImplementation implements StatPerson2PersonService {

    private final StatPerson2PersonRepository statPerson2PersonRepository;
    private final ReportPerson2PersonRequestRepository reportPerson2PersonRequestRepository;

    @Override
    public void savePerson2PersonStatistics(StatPerson2PersonEntity statPerson2PersonEntity, String creator) {
        statPerson2PersonEntity.setCreatedAt(new Date());
        statPerson2PersonEntity.setCreatedBy(creator);
        statPerson2PersonRepository.save(statPerson2PersonEntity);
    }

    @Override
    public Optional<StatPerson2PersonEntity> findLastPerson2PersonStatisticsRecord() {
        return statPerson2PersonRepository.findFirstByOrderByIdDesc();
    }

    @Override
    public void deletePerson2PersonStatisticsByDate(String persianDate) {
        log.info("Starting delete person2person statistics by Persian date: {}", persianDate);
        Integer countRowsAffected = statPerson2PersonRepository.deleteByPersianCalcDate(persianDate);
        log.info("Finished delete person2person statistics by Persian date: {}, deleted rows: {}", persianDate, countRowsAffected);
    }

    @SchedulerLock(
            name = Constant.STAT_PERSON2PERSON_JOB_LOCK,
            lockAtLeastFor = "PT15S", // lock for at least 15 seconds
            lockAtMostFor = "PT3600S" // lock for at most 1 hour
    )
    @Override
    public void generatePerson2PersonStatistics(Date fromDate, Date toDate, String creator) {
        log.info("Starting person2person statistics generation from date: {} to date: {}", fromDate, toDate);

        Optional<StatPerson2PersonEntity> lastPerson2PersonRecord = findLastPerson2PersonStatisticsRecord();

        if (fromDate == null && lastPerson2PersonRecord.isEmpty()) {
            // For first run, start from 30 days ago
            fromDate = DateUtils.getNPreviousDay(new Date(), 30);
        } else if (fromDate == null) {
            // Start from 2 days before the last record to ensure no gaps
            Date lastGeorgianDateRecord = DateUtils.parse(lastPerson2PersonRecord.get().getPersianCalcDate(), PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            fromDate = DateUtils.getNPreviousDay(lastGeorgianDateRecord, 2);
        }

        log.info("Processing person2person statistics from date: {}", fromDate);

        Date currentDate = fromDate;
        while (!currentDate.after(toDate)) {
            List<ReportPerson2PersonRequestRepository.Person2PersonStatPerDay> statPerDayList =
                    reportPerson2PersonRequestRepository.findPerson2PersonAggregationPerDay(currentDate);

            log.info("Found {} person2person statistics records for date: {}", statPerDayList.size(), currentDate);

            if (statPerDayList.isEmpty()) {
                currentDate = DateUtils.getNextDay(currentDate);
                continue;
            }

            // Delete existing records for this date to avoid duplicates
            String persianDate = formatToPersianDate(currentDate);
            deletePerson2PersonStatisticsByDate(persianDate);

            // Save new statistics
            for (ReportPerson2PersonRequestRepository.Person2PersonStatPerDay row : statPerDayList) {
                String persianCalcDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE), DEFAULT_DATE_FORMAT, false);
                Date georgianDate = DateUtils.parse(row.getCreateDateAt(), "yyyy-MM-dd", true, DateUtils.ENGLISH_LOCALE);

                StatPerson2PersonEntity statPerson2PersonEntity = new StatPerson2PersonEntity(
                        row.getChannelId(),
                        row.getCurrencyId(),
                        String.valueOf(row.getResult()),
                        row.getCount(),
                        java.math.BigDecimal.valueOf(row.getAmount()),
                        persianCalcDate,
                        georgianDate
                );

                savePerson2PersonStatistics(statPerson2PersonEntity, creator);
            }

            currentDate = DateUtils.getNextDay(currentDate);
        }

        log.info("Finished person2person statistics generation");
    }

    private String formatToPersianDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy/MM/dd").format(date);
    }
}
