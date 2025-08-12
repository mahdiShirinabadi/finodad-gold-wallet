package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.SadadOperationService;
import com.melli.wallet.service.operation.ShahkarInfoOperationService;
import com.melli.wallet.service.repository.ShahkarInfoRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ShahkarInfoOperationServiceImplementation implements ShahkarInfoOperationService {

    private final ShahkarInfoRepositoryService shahkarInfoRepositoryService;
    private final SadadOperationService sadadOperationService;

    @Override
    public Boolean checkShahkarInfo(String mobileNumber, String nationalCode, boolean isNew) throws InternalServiceException {

        log.info("start check shahkar for nationalCode ({}), mobileNumber ({}), isNew ({})", nationalCode, mobileNumber, isNew);
        Optional<ShahkarInfoEntity> shahkarInfoEntityOptional = shahkarInfoRepositoryService.findTopByMobileAndNationalCodeAndISMatchOrderById(nationalCode, mobileNumber, true);
        if (shahkarInfoEntityOptional.isPresent() && !isNew) {
            log.info("info shahkar for nationalCode ({}), mobile ({}) exist in table with id ({})", nationalCode, mobileNumber, shahkarInfoEntityOptional.get().getId());
            return shahkarInfoEntityOptional.get().getIsMatch();
        }
        log.info("info shahkar for nationalCode ({}), mobile ({}) not exist in table and start read from webService", nationalCode, mobileNumber);
        ShahkarInfoEntity shahkarInfoEntity = new ShahkarInfoEntity();
        shahkarInfoEntity.setCreatedAt(new Date());
        shahkarInfoEntity.setNationalCode(nationalCode);
        shahkarInfoEntity.setMobile(mobileNumber);
        shahkarInfoEntity.setCreatedAt(new Date());
        shahkarInfoEntity.setCreatedBy("System");
        shahkarInfoEntity.setChannelRequestTime(new Date());
        return sadadOperationService.shahkar(shahkarInfoEntity);
    }
}
