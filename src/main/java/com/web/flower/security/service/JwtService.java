package com.web.flower.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.security.JwtProperties;
import com.web.flower.security.domain.UserEntityDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static String createAccessToken(UserEntity userEntity){

        return JWT.create()
                .withSubject("access_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+ JwtProperties.SEC * 20))
                .withClaim("id", userEntity.getId().toString())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }

    public static String createRefreshToken(UserEntity userEntity){

        return JWT.create()
                .withSubject("refresh_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.REFRESH_EXPIRATION_TIME))
                .withClaim("id", userEntity.getId().toString())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }
}
