package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import com.melli.wallet.domain.master.entity.SellCollateralRequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellCollateralRequestRepository extends CrudRepository<SellCollateralRequestEntity, Long> {
    SellCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<SellCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    SellCollateralRequestEntity findById(long requestId);
    List<SellCollateralRequestEntity> findByCreateCollateralRequestEntity(CreateCollateralRequestEntity createCollateralRequestEntity);
}
