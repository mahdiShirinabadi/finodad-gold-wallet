package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.StatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends CrudRepository<StatusEntity, Long> {

    StatusEntity findByCode(String code);

    Page<StatusEntity> findAll(Specification<StatusEntity> spec, Pageable pageable);

    Optional<StatusEntity> findByPersianDescription(String persianDescription);

}
