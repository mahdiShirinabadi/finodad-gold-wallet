package com.melli.hub.domain.response.profile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: ProfileObject
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Setter
@Getter
@ToString
public class ProfileObject {

    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String birthDate;
    private String mobile;
    private String email;
    private String twoFactorAuthentication;
}
