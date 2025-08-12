package com.melli.wallet.service.operation.impl;


import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageOperationService;
import com.melli.wallet.service.operation.SadadOperationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MessageOperationServiceImplementation implements MessageOperationService {

    private SadadOperationService sadadOperationService;

    @Override
    @Async
    public void send(String message, String mobile) throws InternalServiceException {
        try{
            sadadOperationService.sendSms(message, mobile);
        }catch (InternalServiceException ex){
            log.error("error in send sms to sadad and error is ({}), status ({})", ex.getMessage(), ex.getStatus());
            throw ex;
        }
    }

    @Autowired
    public void setSadadService(@Lazy SadadOperationService sadadOperationService) {
        this.sadadOperationService = sadadOperationService;
    }
}
