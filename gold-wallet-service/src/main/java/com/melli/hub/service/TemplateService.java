package com.melli.hub.service;

import com.melli.hub.domain.master.entity.TemplateEntity;

import java.util.List;


public interface TemplateService {

	String CASH_IN = "cash_in";

	String getTemplate(String name);

	List<TemplateEntity> findAllTemplate();

}
