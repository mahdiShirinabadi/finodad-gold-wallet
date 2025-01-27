package com.melli.hub.domain.master.persistence;


import com.melli.hub.domain.master.entity.RequestTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestTypeRepository extends CrudRepository<RequestTypeEntity, Long> {
    RequestTypeEntity findByName(String name);
}
