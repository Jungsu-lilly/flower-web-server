package com.web.flower.security.auth;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.domain.refresh_token.entity.RefreshToken;
import com.web.flower.domain.refresh_token.repository.RefreshTokenRepository;
import com.web.flower.utils.JwtUtils;
import com.web.flower.domain.message.Message;
import com.web.flower.utils.CookieUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtUtils jwtUtils;

    private BasicAuthenticationConverter authenticationConverter = new BasicAuthenticationConverter();

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtUtils jwtUtils) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = null;

        try {
            Cookie cookie = Arrays.stream(request.getCookies())
                    .filter(r -> r.getName().equals("flower_token"))
                    .findAny()
                    .orElse(null);

            accessToken = cookie.getValue();
        } catch (Exception e) { // 엑세스토큰이 존재 X
            chain.doFilter(request, response);
            return;
        }

        boolean isAccessTokenExpired = false;

        try {
            String username = jwtUtils.validateToken(accessToken);
        } catch (TokenExpiredException e) {
            isAccessTokenExpired = true;
        } catch (SignatureVerificationException e) { // 토큰 서명 오류
            chain.doFilter(request, response);
            return;
        }

        if (isAccessTokenExpired) {
            String userId = jwtUtils.getUserIdFromToken(accessToken);
            Optional<User> findUser = userRepository.findById(UUID.fromString(userId));
            User user = findUser.get();

            Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findByUserId(UUID.fromString(userId));
            if (!findRefreshToken.isPresent()) { // 리프레시 토큰이 없다면
                makeResponse(request, response, HttpStatus.NOT_FOUND, "fail", "재인증(로그인)이 필요합니다.");
                chain.doFilter(request, response);
                return;
            }

            RefreshToken refreshToken = findRefreshToken.get();
            try {
                String refreshTokenValue = refreshToken.getValue();
                jwtUtils.validateToken(refreshTokenValue);
            } catch (TokenExpiredException e) { // 리프레시 토큰 만료
                refreshTokenRepository.delete(refreshToken);
                chain.doFilter(request, response);
                return;
            }

            /**
             * 리프레시 토큰 유효함. 엑세스토큰 재발급 **/
            String newAccessToken = jwtUtils.createAccessToken(user);
            CookieUtils.makeCookie(accessToken, response);
        }

        String userId = jwtUtils.getUserIdFromToken(accessToken);
        User user = userRepository.findById(UUID.fromString(userId)).get();

        PrincipalDetails principalDetails = new PrincipalDetails(user);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        chain.doFilter(request, response);
    }

    public void makeResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String msg, String memo) throws IOException, ServletException {
        ObjectMapper om = new ObjectMapper();
        Message message = Message.builder()
                .message(msg)
                .status(status)
                .memo(memo)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON.toString());
        om.writeValue(response.getOutputStream(), message);
    }
}
