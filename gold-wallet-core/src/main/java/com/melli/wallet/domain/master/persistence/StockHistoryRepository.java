package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.StockHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockHistoryRepository extends CrudRepository<StockHistoryEntity, Long> {
    Page<StockHistoryEntity> findAll(Specification<StockHistoryEntity> spec, Pageable pageable);
}
