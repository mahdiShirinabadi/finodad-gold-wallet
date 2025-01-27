package com.melli.hub.service;

import com.melli.hub.domain.master.entity.RequestTypeEntity;
import com.melli.hub.exception.InternalServiceException;
import com.tara.wallet.exception.ServiceException;
import com.tara.wallet.master.domain.RequestType;
import com.tara.wallet.response.RequestTypeListResponse;
import com.tara.wallet.response.RequestTypeResponseReport;
import com.tara.wallet.response.Response;
import com.tara.wallet.slave.domain.ReportRequestType;

import java.util.List;

/**
 * Created by shirinabadi on 03/11/2016.
 */
public interface RequestTypeService {
	String TRACE_ID = "trace_id";
	String CASH_IN = "cash_in";
	RequestTypeEntity getRequestType(String name);
	void clearCache (long id)throws InternalServiceException;
	void clearCacheAllData();
}
