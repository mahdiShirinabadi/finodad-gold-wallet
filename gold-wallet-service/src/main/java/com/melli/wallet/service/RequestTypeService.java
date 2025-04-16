package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.exception.InternalServiceException;

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
