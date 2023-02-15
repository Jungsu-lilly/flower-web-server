package com.web.flower.security.oauth.provider;

public interface OAuth2UserInfo {

    String getProvider();
    String getProviderId();
    String getProfileName();
    String getEmail();
    String getProfileImagePath();
}
