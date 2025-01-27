package com.melli.hub.service.impl;

import com.melli.hub.service.RequestTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.tara.wallet.util.CashNameConstant.REQUEST_TYPE_SERVICE_NAME;


@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = REQUEST_TYPE_SERVICE_NAME)
public class RequestTypeServiceImplementation implements RequestTypeService {

    private final RequestTypeRe requestTypeDAO;




    @Override
    public void init() {
        log.info("Start load type .....");
    }

    @Override
    @Cacheable(unless = "#result == null")
    public RequestType getRequestType(String name) {
        log.info("start get RequestType for name ==> ({})", name);
        return requestTypeDAO.findByName(name);
    }

    @Override
    public ReportRequestType getReportRequestType(String name) {
        log.info("start get ReportRequestType for name ==> ({})", name);
        return reportRequestTypeDAO.findByName(name);
    }


    @Override
    public void clearCache(long id) throws ServiceException{
        log.info("start clear RequestType id ==> ({})", id);
        RequestType requestType = findById(id);
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

    private RequestType findById(long id) throws ServiceException{
        Optional<RequestType> requestType=requestTypeDAO.findById(id);
        if(requestType.isEmpty()){
            log.debug("request Type with Id ({}), not found !!! ", id);
            throw new ServiceException("request Type with Id (" + id + ") not found !!!", RequestService.REQUEST_TYPE_NOT_FOUND);
        }

        return requestType.get();
    }


    @Override
    public List<RequestType> findAllRequestType() {
        return requestTypeDAO.findAll();
    }

    @Override
    public Response createRequestType(String name, String faName, int display) {
        log.info("start call createRequestType with parameters :name = {} faName = {} , display = {} ", name, faName, display);
        RequestType requestType = new RequestType();
        requestType.setName(name);
        requestType.setFaName(faName);
        requestType.setDisplay(display);
        requestTypeDAO.save(requestType);
        log.info("save createRequestType...");
        return helper.fillResponse(RequestService.SUCCESSFUL,"");
    }


    @Override
    public RequestTypeResponseReport getRequestTypeList(Integer page, Integer size) {
        log.info("start call getRequestTypeList with parameters : page = {} , size = {} ", page, size);
        SettingValueResponse settingValueResponse = settingService.setDefaultPageAndSize(page, size);
        log.info(" in getRequestTypeList service -> page and size for use in query => {} , {} ", settingValueResponse.getPage(), settingValueResponse.getSize());
        Pageable pageRequest = PageRequest.of(settingValueResponse.getPage(), settingValueResponse.getSize(), Sort.by("id").descending());

        Page<ReportRequestType> requestTypeList = reportRequestTypeDAO.findAllByDisplay(pageRequest, 1);
        log.info(" in getRequestTypeList with out parameters ... ");
        return helper.fillRequestTypeList(requestTypeList.getContent(), requestTypeList.getTotalElements(), requestTypeList.getTotalPages(), requestTypeList.getNumberOfElements(), RequestService.SUCCESSFUL);
    }

    public RequestTypeListResponse getRequestTypeList(){
        log.info("start call getRequestTypeList  ");
        List<ReportRequestType>reportRequestTypeList=reportRequestTypeDAO.findAll();
        return helper.fillRequestTypeList(reportRequestTypeList, RequestService.SUCCESSFUL);
    }


}
