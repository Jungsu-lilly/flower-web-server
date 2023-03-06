package com.web.flower.domain.social_login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private UUID id;
    private String name;
    private String imageUrl;
    private String role;
    private String tokenType;
    private String accessToken;
    private String refreshToken;
}
