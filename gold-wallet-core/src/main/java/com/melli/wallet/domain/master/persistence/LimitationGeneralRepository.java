package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.LimitationGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitationGeneralRepository extends CrudRepository<LimitationGeneralEntity, Long> {
    LimitationGeneralEntity findByNameAndEndTimeIsNull(String name);
    LimitationGeneralEntity findByName(String name);
    Page<LimitationGeneralEntity> findAll(Specification<LimitationGeneralEntity> spec, Pageable pageable);
    List<LimitationGeneralEntity> findAll();
}
