package com.melli.hub.security;

import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class JwtProfileDetailsService implements UserDetailsService {

    private final ProfileService profileService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ProfileEntity profileEntity = profileService.findByNationalCode(username);
        if (profileEntity == null) {
            log.error("user with nationalCode ({}) not found", username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return profileEntity;
    }
}
