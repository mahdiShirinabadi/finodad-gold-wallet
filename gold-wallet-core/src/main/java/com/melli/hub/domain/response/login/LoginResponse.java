package com.melli.hub.domain.response.login;

import com.melli.hub.domain.response.profile.ProfileObject;
import lombok.*;

/**
 * Created by shirinabadi on 1/23/2017.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginResponse {
    private boolean twoFactorAuthentication;
    private ProfileObject profileObject;
    private TokenObject accessTokenObject;
    private TokenObject refreshTokenObject;
}
