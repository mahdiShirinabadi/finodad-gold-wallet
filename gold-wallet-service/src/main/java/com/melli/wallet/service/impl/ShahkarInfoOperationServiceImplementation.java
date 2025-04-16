package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.SadadService;
import com.melli.wallet.service.ShahkarInfoOperationService;
import com.melli.wallet.service.ShahkarInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ShahkarInfoOperationServiceImplementation implements ShahkarInfoOperationService {

    private final ShahkarInfoService shahkarInfoService;
    private final SadadService sadadService;

    @Override
    public Boolean checkShahkarInfo(String mobileNumber, String nationalCode, boolean isNew) throws InternalServiceException {

        log.info("start check shahkar for nationalCode ({}), mobileNumber ({}), isNew ({})", nationalCode, mobileNumber, isNew);
        Optional<ShahkarInfoEntity> shahkarInfoEntityOptional = shahkarInfoService.findTopByMobileAndNationalCodeAndISMatchOrderById(nationalCode, mobileNumber, true);
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
        return sadadService.shahkar(shahkarInfoEntity);
    }
}
