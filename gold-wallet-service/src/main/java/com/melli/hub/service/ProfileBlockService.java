package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ProfileBlockEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;

public interface ProfileBlockService {

	ProfileBlockEntity findByProfile(ProfileEntity profileEntity);

	void save(ProfileBlockEntity profileBlockEntity);

	void clearCache();

}
