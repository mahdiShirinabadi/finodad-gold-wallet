package com.melli.hub.domain.master.persistence;


import com.melli.hub.domain.master.entity.TemplateEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends CrudRepository<TemplateEntity, Long> {
    TemplateEntity findByName(String name);
}
