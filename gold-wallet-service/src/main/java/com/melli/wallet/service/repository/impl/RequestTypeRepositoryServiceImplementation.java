package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.domain.master.persistence.RequestTypeRepository;
import com.melli.wallet.domain.slave.entity.ReportRequestTypeEntity;
import com.melli.wallet.domain.slave.persistence.ReportRequestTypeRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.mapper.RequestTypeMapper;
import com.melli.wallet.service.repository.RequestTypeRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
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
@CacheConfig(cacheNames = ConstantRedisName.WALLET_REQUEST_TYPE)
public class RequestTypeRepositoryServiceImplementation implements RequestTypeRepositoryService {

    private final ReportRequestTypeRepository reportRequestTypeRepository;
    private final RequestTypeMapper requestTypeMapper;

    @Override
    @Cacheable(unless = "#result == null")
    public RequestTypeEntity getRequestType(String name) {
        log.info("start get RequestType for name ==> ({})", name);
        ReportRequestTypeEntity reportEntity = reportRequestTypeRepository.findByName(name.toLowerCase());
        return requestTypeMapper.toRequestTypeEntity(reportEntity);
    }


    @Override
    @Cacheable(unless = "#result == null")
    public RequestTypeEntity getRequestTypeById(long id) {
        log.info("start get RequestType for id ==> ({})", id);
        ReportRequestTypeEntity reportEntity = reportRequestTypeRepository.findById(id);
        return requestTypeMapper.toRequestTypeEntity(reportEntity);
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
        ReportRequestTypeEntity requestType=reportRequestTypeRepository.findById(id);
        if(requestType == null){
            log.debug("request Type with Id ({}), not found !!! ", id);
            throw new InternalServiceException("request Type with Id (" + id + ") not found !!!", StatusRepositoryService.REQUEST_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return requestTypeMapper.toRequestTypeEntity(requestType);
    }



}
