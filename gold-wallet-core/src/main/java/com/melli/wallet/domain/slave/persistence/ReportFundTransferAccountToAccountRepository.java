package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportFundTransferAccountToAccountRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportFundTransferAccountToAccountRepository extends CrudRepository<ReportFundTransferAccountToAccountRequestEntity, Long> {

    /**
     * Find FundTransferAccountToAccountRequest by request id
     * Uses JPA query to properly handle JOINED inheritance with parent request table
     */
    @Query("SELECT ft FROM ReportFundTransferAccountToAccountRequestEntity ft WHERE ft.id = :id")
    ReportFundTransferAccountToAccountRequestEntity findFundTransferById(@Param("id") long id);
}
