package com.web.flower.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.message.Message;
import com.web.flower.domain.refresh_token.entity.RefreshToken;
import com.web.flower.domain.refresh_token.repository.RefreshTokenRepository;
import com.web.flower.domain.refresh_token.service.JwtService;
import com.web.flower.domain.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

// 스프링 시큐리티에서 UsernamePasswordAuthenticationFilter 가 있다.
// /login 요청해서 username, password POST 전송하면 위 필터가 동작한다.
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("=== [AuthenticationFilter] 인증(로그인) 요청 도착 ===");

        try {
            ObjectMapper om = new ObjectMapper();
            User userEntity = om.readValue(request.getInputStream(), User.class);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;
        } catch (IOException e) {
            throw new AuthenticationServiceException("username, password 가 일치하지 않습니다.");
        }
    }

    // attemptAuthentication 실행 후, 인증이 정상적으로 되었다면 successfulAuthentication 함수가 실행된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("=== 사용자 인증 성공 ===");
        System.out.println("=== successfulAuthentication() ===");
        User userEntity = ((PrincipalDetails) authResult.getPrincipal()).getUser();

        String accessToken = jwtService.createAccessToken(userEntity);
        String refreshToken = jwtService.createRefreshToken(userEntity);

        // refresh Token DB에 저장. 이미 존재하는 RefreshToken 이 있다면, 제거 후 생성
        Optional<RefreshToken> findToken = refreshTokenRepository.findByUserId(userEntity.getId());
        if(findToken.isPresent()){
            refreshTokenRepository.delete(findToken.get());
        }

        RefreshToken buildToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .value(refreshToken)
                .userId(userEntity.getId())
                .build();
        refreshTokenRepository.save(buildToken);
        System.out.println("refreshToken = " + refreshToken);

        Cookie cookie = new Cookie("flower_token",accessToken);
        cookie.setMaxAge(60*60*3); // 3시간
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        Message message = Message.builder()
                .status(HttpStatus.OK)
                .message("success")
                .memo("로그인 성공!")
                .build();

        ObjectMapper om = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        om.writeValue(response.getOutputStream(), message);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        System.out.println("=== unsuccessfulAuthentication 실행됨 =====");

        Object exceptionType = failed.getClass();

        if(exceptionType.equals(BadCredentialsException.class) || exceptionType.equals(UsernameNotFoundException.class)){
            ObjectMapper om = new ObjectMapper();

            Message message = Message.builder()
                    .message("auth_fail")
                    .status(HttpStatus.UNAUTHORIZED)
                    .memo(failed.getLocalizedMessage())
                    .build();

            response.setContentType(MediaType.APPLICATION_JSON.toString());
            om.writeValue(response.getOutputStream(),message);
        }
    }
}
