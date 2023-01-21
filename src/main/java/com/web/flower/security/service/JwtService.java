package com.web.flower.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.web.flower.security.auth.UserEntityDetails;
import com.web.flower.security.jwt.JwtProperties;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    public static String createAccessToken(UserEntityDetails userEntityDetails){

        return JWT.create()
                .withSubject("access_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+ JwtProperties.SEC * 20))
                .withClaim("id", userEntityDetails.getUserEntity().getId().toString())
                .withClaim("username", userEntityDetails.getUserEntity().getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }

    public static String createRefreshToken(UserEntityDetails userEntityDetails){

        return  JWT.create()
                .withSubject("refresh_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.REFRESH_EXPIRATION_TIME))
                .withClaim("id", userEntityDetails.getUserEntity().getId().toString())
                .withClaim("username", userEntityDetails.getUserEntity().getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }
}
