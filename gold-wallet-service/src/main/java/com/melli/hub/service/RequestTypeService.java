package com.melli.hub.service;

import com.melli.hub.domain.master.entity.RequestTypeEntity;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

/**
 * Created by shirinabadi on 03/11/2016.
 */
public interface RequestTypeService {
	String TRACE_ID = "trace_id";
	String CASH_IN = "cash_in";
	String CASH_OUT = "cash_out";
	String PURCHASE = "purchase";
	RequestTypeEntity getRequestType(String name);
	void clearCache (long id)throws InternalServiceException;
	void clearCacheAllData();
}
