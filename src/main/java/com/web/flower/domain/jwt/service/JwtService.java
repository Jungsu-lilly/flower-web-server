package com.web.flower.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.web.flower.domain.user.entity.User;

import com.web.flower.security.config.auth.PrincipalDetails;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;

@Service
@NoArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.token-validity-in-seconds}")
    private long expirationLength;

    public JwtService( @Value("${jwt.secret}") String secret,
                       @Value("${jwt.token-validity-in-seconds}") long expirationLength) {
        this.secret = secret;
        this.expirationLength = expirationLength;
    }

    public String createAccessToken(User userEntity){

        return JWT.create()
                .withSubject("access_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+ expirationLength*30))
                .withClaim("id", userEntity.getId().toString())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(secret));
    }

    public String createRefreshToken(User userEntity){

        return JWT.create()
                .withSubject("access_token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+ expirationLength *45))
                .withClaim("id", userEntity.getId().toString())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(secret));
    }

    public static void makeCookie(HttpServletResponse response, String jwtToken, int time) {
        Cookie cookie = new Cookie("Authorization", jwtToken);
        cookie.setMaxAge(time);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
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

    private void issueNewAccessToken(HttpServletResponse response, User user) {
        System.out.println("새 AccessToken 발급");
        String newAccessToken = createAccessToken(user);
        String jwtToken = createAccessToken(user);

        JwtService.makeCookie(response, jwtToken, 60*30);
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(principalDetails, null,principalDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
