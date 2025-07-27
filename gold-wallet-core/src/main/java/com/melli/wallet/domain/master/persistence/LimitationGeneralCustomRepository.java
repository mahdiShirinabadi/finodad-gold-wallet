package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.LimitationGeneralCustomEntity;
import com.melli.wallet.domain.master.entity.LimitationGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitationGeneralCustomRepository extends CrudRepository<LimitationGeneralCustomEntity, Long> {

    List<LimitationGeneralCustomEntity> findByLimitationGeneralEntityAndEndTimeIsNull(LimitationGeneralEntity limitationGeneralEntity);
    List<LimitationGeneralCustomEntity> findByEndTimeIsNull();
    Page<LimitationGeneralCustomEntity> findAll(Specification<LimitationGeneralCustomEntity> spec, Pageable pageable);
}
