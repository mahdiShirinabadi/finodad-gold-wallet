package com.melli.hub.service;


import com.melli.hub.domain.master.entity.ShahkarInfoEntity;

import java.util.Optional;

public interface ShahkarInfoService {
    void save(ShahkarInfoEntity shahkarInfoEntity);
    Optional<ShahkarInfoEntity> findTopByMobileAndNationalCodeAndISMatchOrderById(String mobile, String nationalCode, Boolean isMatch);
}
