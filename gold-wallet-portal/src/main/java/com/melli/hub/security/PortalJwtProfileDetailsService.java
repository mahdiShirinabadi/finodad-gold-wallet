package com.melli.hub.security;

import com.melli.hub.service.PanelOperatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PortalJwtProfileDetailsService implements UserDetailsService {

    private final PanelOperatorService panelOperatorService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            PanelOperatorEntity panelOperatorEntity = panelOperatorService.findByUsername(username);
            if(panelOperatorEntity == null){
                log.error("user with username ({}) not found", username);
                throw new UsernameNotFoundException("User not found with email: " + username);
            }
            return panelOperatorEntity;
    }
}
