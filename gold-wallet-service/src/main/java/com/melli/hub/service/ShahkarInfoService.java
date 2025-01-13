package com.melli.hub.service;


import java.util.Optional;

public interface ShahkarInfoService {
    void save(ShahkarInfoEntity shahkarInfoEntity);
    Optional<ShahkarInfoEntity> findTopByMobileAndNationalCodeAndISMatchOrderById(String mobile, String nationalCode, Boolean isMatch);
}
