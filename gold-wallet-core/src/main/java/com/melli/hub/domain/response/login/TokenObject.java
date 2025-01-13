package com.melli.hub.domain.response.login;

import lombok.*;

/**
 * Class Name: AccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/8/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TokenObject {

    private String token;
    private long expireTime;
}
