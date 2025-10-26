package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.StatBuyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface StatBuyRepository extends JpaRepository<StatBuyEntity, Long> {

    /**
     * Find the last record by ID descending order
     */
    Optional<StatBuyEntity> findFirstByOrderByIdDesc();

    /**
     * Delete records by Persian calculation date
     */
    @Modifying
    @Query("DELETE FROM StatBuyEntity s WHERE s.persianCalcDate = :persianDate")
    Integer deleteByPersianCalcDate(@Param("persianDate") String persianDate);

    /**
     * Find statistics by create time range
     */
    @Query("SELECT s FROM StatBuyEntity s WHERE s.georgianCalcDate BETWEEN :fromDate AND :toDate ORDER BY s.georgianCalcDate DESC")
    java.util.List<StatBuyEntity> findByCreateTime(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    /**
     * Find all statistics ordered by Georgian calculation date
     */
    @Query("SELECT s FROM StatBuyEntity s ORDER BY s.georgianCalcDate DESC")
    java.util.List<StatBuyEntity> findAllStat();

    Page<StatBuyEntity> findAll(Specification<StatBuyEntity> specification, Pageable pageable);
}
