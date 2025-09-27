package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreateCollateralRequestRepository extends CrudRepository<CreateCollateralRequestEntity, Long> {

    CreateCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<CreateCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    CreateCollateralRequestEntity findById(long requestId);
    Optional<CreateCollateralRequestEntity> findByCode(String code);

    @Query("select count(a.id) from CreateCollateralRequestEntity a where a.code = :code")
    Long countByCode(@Param("code") String code);
}
