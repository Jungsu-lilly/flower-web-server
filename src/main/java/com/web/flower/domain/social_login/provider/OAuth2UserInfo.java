package com.web.flower.domain.social_login.provider;

public interface OAuth2UserInfo {

    String getProvider();
    String getProviderId();
    String getProfileName();
    String getEmail();
    String getProfileImagePath();
}
