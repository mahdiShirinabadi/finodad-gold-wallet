package com.melli.wallet.grpc.security;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class JwtChannelDetailsService implements UserDetailsService {

    private final ChannelService channelService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ChannelEntity channelEntity = channelService.findByUsername(username);
        if (channelEntity == null) {
            log.error("user with nationalCode ({}) not found", username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return channelEntity;
    }
}
