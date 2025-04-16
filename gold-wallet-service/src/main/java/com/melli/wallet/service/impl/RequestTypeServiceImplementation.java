package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.domain.master.persistence.RequestTypeRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.RequestTypeService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = Constant.WALLET_REQUEST_TYPE)
public class RequestTypeServiceImplementation implements RequestTypeService {

    private final RequestTypeRepository requestTypeDAO;

    @Override
    @Cacheable(unless = "#result == null")
    public RequestTypeEntity getRequestType(String name) {
        log.info("start get RequestType for name ==> ({})", name);
        return requestTypeDAO.findByName(name);
    }



    @Override
    public void clearCache(long id) throws InternalServiceException {
        log.info("start clear RequestType id ==> ({})", id);
        RequestTypeEntity requestType = findById(id);
        clearCacheData(requestType.getName());

    }


    @CacheEvict
    public void clearCacheData(String name) {
        log.info("start clear RequestType ==> ({})", name);
    }


    @CacheEvict(allEntries = true)
    @Override
    public void clearCacheAllData() {
        log.info("start clear all RequestType");
    }

    private RequestTypeEntity findById(long id) throws InternalServiceException{
        Optional<RequestTypeEntity> requestType=requestTypeDAO.findById(id);
        if(requestType.isEmpty()){
            log.debug("request Type with Id ({}), not found !!! ", id);
            throw new InternalServiceException("request Type with Id (" + id + ") not found !!!", StatusService.REQUEST_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return requestType.get();
    }



}
