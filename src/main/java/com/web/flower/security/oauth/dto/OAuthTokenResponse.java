package com.web.flower.security.oauth.dto;

import lombok.Data;

import java.util.Map;

@Data
public class OAuthTokenResponse {

    String access_token;
    String token_type;
    String refresh_token;
    String expires_in;
    String scope;
    String refresh_token_expires_in;
}
