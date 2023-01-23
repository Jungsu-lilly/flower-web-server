package com.web.flower.security.validate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;

import java.util.Base64;

public class TokenValidator {

    public static String validateToken(String secret, String token) throws TokenExpiredException {
        String username = JWT.require(Algorithm.HMAC512(secret)).build()
                .verify(token)
                .getClaim("username").asString();
        return username;
    }

    public static String getUserIdFromToken(String token){
        String[] chunks = token.split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));

        String tmp = payload.split(",")[1].split(":")[1];
        String userId = tmp.substring(1, tmp.length() - 1);
        return userId;
    }
}
