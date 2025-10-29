package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.FundTransferAccountToAccountRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundTransferAccountToAccountRepository extends CrudRepository<FundTransferAccountToAccountRequestEntity, Long> {
    Optional<FundTransferAccountToAccountRequestEntity> findByRrnEntity(RrnEntity rrn);
}
