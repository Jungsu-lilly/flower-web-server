package com.web.flower.security.config.oauth.provider;

public interface OAuth2UserInfo {

    String getProvider();
    String getProviderId();
    String getEmail();
    String getUsername();
    String getProfileImagePath();
}
