package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import com.melli.wallet.domain.master.entity.ReleaseCollateralRequestEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseCollateralRequestRepository extends CrudRepository<ReleaseCollateralRequestEntity, Long> {

    @Query(value = "select request_id from {h-schema}release_collateral_request p where p.rrn_id=:traceId", nativeQuery = true)
    Long findByRrnEntityIdNative(@Param("traceId") long traceId);

    ReleaseCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<ReleaseCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    ReleaseCollateralRequestEntity findById(long requestId);
    List<ReleaseCollateralRequestEntity> findByCreateCollateralRequestEntity(CreateCollateralRequestEntity createCollateralRequestEntity);
}
