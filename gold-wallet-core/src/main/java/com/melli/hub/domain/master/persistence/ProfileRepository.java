package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ProfileAccessTokenEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileRepository extends CrudRepository<ProfileEntity, Long> {
    ProfileEntity findByNationalCode(String nationalCode);
}
