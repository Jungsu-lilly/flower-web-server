package com.web.flower.domain.social_login.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginReqDto {

    private String provider;
    private String code;
}