package com.web.flower.security.filter;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.config.auth.PrincipalDetails;
import com.web.flower.security.config.auth.PrincipalUserDetailsService;
import com.web.flower.security.domain.RefreshToken;
import com.web.flower.security.repository.RefreshTokenRepository;
import com.web.flower.security.service.JwtService;
import com.web.flower.security.domain.Message;
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
import java.util.stream.Collectors;

// 시큐리티가 필터를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 가 있음.
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 안탄다.
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;

    private BasicAuthenticationConverter authenticationConverter = new BasicAuthenticationConverter();

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }


    // 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨
    // JWT 를 체크해 부합하면 권한을 부여한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("=== [Authorization Filter] 인증이나 권한이 필요한 주소 요청됨 ===");

        Cookie cookie = null;
        try {
            cookie = Arrays.stream(request.getCookies())
                    .filter(r -> r.getName().equals("Authorization"))
                    .findAny()
                    .orElse(null);
        } catch (Exception e) { // 엑세스토큰이 존재 X
            chain.doFilter(request, response);
            return;
        }
        String accessToken = "";
        try{
            accessToken = cookie.getValue();
        }
        catch (NullPointerException e){
            chain.doFilter(request, response);
            return;
        }
        System.out.println("accessToken = " + accessToken);
        boolean isAccessTokenExpired = false;

        String username = null;
        try {
            username = jwtService.validateToken(accessToken);
        } catch (TokenExpiredException e) {
            // 엑세스토큰 만료
            System.out.println("=== Access Token Expired ======");
            isAccessTokenExpired = true;
        } catch (SignatureVerificationException e) {
            // 토큰 서명 오류
            System.out.println("=== 토큰값 서명이 올바르지 않습니다. 다시 입력해주세요 ======");
            chain.doFilter(request, response);
            return;
        }

        System.out.println("username = " + username);
        User user = userRepository.findByUsername(username);

        if (isAccessTokenExpired) { // 엑세스 토큰이 만료된 경우
            System.out.println("엑세스 토큰 만료");
            // 엑세스 토큰에서 userId 추출
            String userId = jwtService.getUserIdFromToken(accessToken);

            // userId로 리프레쉬 토큰을 찾는다.
            Optional<RefreshToken> byUserId = refreshTokenRepository.findByUserId(UUID.fromString(userId));
            if (!byUserId.isPresent()) { // 리프레시 토큰이 없다면
                makeResponse(request, response, HttpStatus.NOT_FOUND, "no refresh_token exists", "재인증(로그인) 필요");
                chain.doFilter(request, response);
                return;
            }
            RefreshToken refreshToken = byUserId.get();
            try {
                String refreshTokenValue = refreshToken.getValue();
                jwtService.validateToken(refreshTokenValue);
            } catch (TokenExpiredException e) { // 리프레시 토큰 만료
                System.out.println("=== 리프레시 토큰 만료. 재인증 필요 =====");
                chain.doFilter(request, response);
                return;
            }
            // 리프레시 토큰이 유효할 때
            // AccessToken 재발급
            System.out.println("=== 새 AccessToken 발급 =====");
            String newAccessToken = jwtService.createAccessToken(user);

            Cookie resCookie = new Cookie("Authorization", newAccessToken);
            resCookie.setMaxAge(20); // 20초
            resCookie.setHttpOnly(true);
            response.addCookie(resCookie);

            makeResponse(request, response, HttpStatus.OK, "access_token_expired", "엑세스토큰을 재발급합니다.");
        }
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
        //response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        om.writeValue(response.getOutputStream(), message);
    }
}
