package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestTypeRepository extends CrudRepository<RequestTypeEntity, Long> {
    RequestTypeEntity findByName(String name);
}
