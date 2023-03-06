package com.web.flower.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.web.flower.domain.user.entity.User;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
@NoArgsConstructor
public class JwtUtils {

    @Value("${flower.app.jwt.secret.key}")
    private String secret;

    private long expirationLength = 1000L;

    public JwtUtils(@Value("${flower.app.jwt.secret.key}") String secret) {
        this.secret = secret;
    }

    public String createAccessToken(User userEntity){

        return JWT.create()
                .withSubject("access_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+ expirationLength*60*15)) // 15분
                .withClaim("id", userEntity.getId().toString())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(secret));
    }

    public String createRefreshToken(User userEntity){

        return JWT.create()
                .withSubject("refresh_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+ expirationLength*60*60*24*3)) // 3일
                .withClaim("id", userEntity.getId().toString())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(secret));
    }

    public String validateToken(String token) throws TokenExpiredException {
        String username = JWT.require(Algorithm.HMAC512(secret)).build()
                .verify(token)
                .getClaim("username").asString();
        return username;
    }

    public String getUserIdFromToken(String token){
        String[] chunks = token.split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));

        String tmp = payload.split(",")[1].split(":")[1];
        String userId = tmp.substring(1, tmp.length() - 1);
        return userId;
    }

}
