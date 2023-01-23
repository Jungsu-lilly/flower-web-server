package com.web.flower.security.filter;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.domain.RefreshToken;
import com.web.flower.security.repository.RefreshTokenRepository;
import com.web.flower.security.service.JwtService;
import com.web.flower.security.domain.UserEntityDetails;
import com.web.flower.security.domain.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String username = null;

        //Predicate<Cookie> authorizationCookiePredicate = (Cookie cookie) -> cookie.getName().equals("Authorization");
        Cookie[] cookies = request.getCookies();
        if(cookies==null){
            chain.doFilter(request, response);
            return;
        }
        List<Cookie> authorizationCookie = Arrays.asList(cookies).stream()
                .filter(cookie -> cookie.getName().equals("Authorization")).collect(Collectors.toList());

        Cookie cookie = authorizationCookie.get(0);
        String accessToken = cookie.getValue();

        if(accessToken == null) {
            chain.doFilter(request, response);
            return;
        }
        try{
            username = jwtService.validateToken(accessToken);
        }
        catch (TokenExpiredException e){
            System.out.println("[AccessToken 만료됨]");
            String userId = jwtService.getUserIdFromToken(accessToken);

            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(UUID.fromString(userId));
            if(!refreshToken.isPresent()){
                //throw new NullPointerException("no refresh Token");
                makeResponse(request, response, HttpStatus.NOT_FOUND,"no refresh_token exists","재인증(로그인) 필요");
                return;
            }
            if (refreshTokenExpired(request, response, refreshToken.get())) {
                chain.doFilter(request, response);
                return;
            }

            Optional<UserEntity> findUser = userRepository.findById(UUID.fromString(userId));
            if(!findUser.isPresent()){
                makeResponse(request, response, HttpStatus.NOT_FOUND, "user not found", "유저를 찾을 수 없습니다.");
                throw new NoSuchElementException();
            }
            UserEntity userEntity = findUser.get();

            System.out.println("새 AccessToken 발급");
            issueNewAccessToken(response, userEntity);
            makeResponse(request, response, HttpStatus.OK,"access_token_expired","엑세스토큰을 재발급합니다.");
        }
        catch (SignatureVerificationException e){
            invalidAccessToken(request, response, chain);
            chain.doFilter(request, response);
            return;
        }

        // 서명이 정상적으로 됨
        if(username!=null){
            UserEntity userEntity = userRepository.findByUsername(username).get();
            // JWT 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
            // 강제로 시큐리티 세션에 접근하여 Authentication 객체를 저장
            setAuthenticationTokenToSecurityContext(userEntity);
        }
        chain.doFilter(request, response);
    }


    private static void invalidAccessToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("Access 토큰값이 유효하지 않습니다. 다시 입력해주세요.");
        makeResponse(request, response, HttpStatus.UNAUTHORIZED,"wrong_token_value", "Access 토큰값이 잘못되었습니다.");
        chain.doFilter(request, response);
        return;
    }

    private boolean refreshTokenExpired(HttpServletRequest request, HttpServletResponse response, RefreshToken refreshToken) throws IOException, ServletException {
        try{
            jwtService.validateToken(refreshToken.getValue());
        }
        catch (TokenExpiredException e1){
            System.out.println("=== RefreshToken 유효기간 만료 ===");
            refreshTokenRepository.delete(refreshToken);
            makeResponse(request, response, HttpStatus.UNAUTHORIZED,"refresh_token_expired", "재인증 필요");
            return true;
        }
        return false;
    }


    private void issueNewAccessToken(HttpServletResponse response, UserEntity userEntity) {
        String newAccessToken = jwtService.createAccessToken(userEntity);
        String jwtToken = jwtService.createAccessToken(userEntity);

        JwtService.makeCookie(response, jwtToken, 60*30);
        setAuthenticationTokenToSecurityContext(userEntity);
    }


    private static void setAuthenticationTokenToSecurityContext(UserEntity userEntity) {
        UserEntityDetails userEntityDetails = new UserEntityDetails(userEntity);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userEntityDetails, null, userEntityDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static void makeResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String msg, String memo) throws IOException, ServletException {
        ObjectMapper om = new ObjectMapper();
        Message message = Message.builder()
                .message(msg)
                .status(status)
                .memo(memo)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON.toString());
        //response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        om.writeValue(response.getOutputStream(),message);
    }

}
