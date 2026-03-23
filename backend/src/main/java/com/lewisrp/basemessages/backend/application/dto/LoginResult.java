package com.lewisrp.basemessages.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result object for successful login operations.
 * Contains the access token, refresh token, and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    /**
     * Inner class representing user information in the login result.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
        private String avatarUrl;
    }
}
