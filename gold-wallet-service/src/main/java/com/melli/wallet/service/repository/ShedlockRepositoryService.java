package com.melli.wallet.service.repository;



import com.melli.wallet.domain.master.entity.ShedLockEntity;
import com.melli.wallet.domain.response.panel.PanelShedlockResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

public interface ShedlockRepositoryService {
    PanelShedlockResponse findAll(Map<String, String> mapParameter);

    ShedLockEntity findByName(String name);
    Integer deleteByName(String name);
    void checkActiveLock(String name) throws InternalServiceException;
}
