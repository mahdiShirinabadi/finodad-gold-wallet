package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidCollateralRequestRepository extends CrudRepository<LiquidCollateralRequestEntity, Long> {
    LiquidCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<LiquidCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    LiquidCollateralRequestEntity findById(long requestId);
    List<LiquidCollateralRequestEntity> findByCreateCollateralRequestEntity(CreateCollateralRequestEntity createCollateralRequestEntity);
}
