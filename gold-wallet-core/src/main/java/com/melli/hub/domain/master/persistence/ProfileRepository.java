package com.melli.hub.domain.master.persistence;

import org.springframework.data.repository.CrudRepository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileRepository extends CrudRepository<ProfileEntity, Long> {
    ProfileEntity findByNationalCode(String nationalCode);
}
