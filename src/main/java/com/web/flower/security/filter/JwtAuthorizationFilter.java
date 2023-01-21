package com.web.flower.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.JwtProperties;
import com.web.flower.security.domain.RefreshToken;
import com.web.flower.security.repository.RefreshTokenRepository;
import com.web.flower.security.service.JwtService;
import com.web.flower.security.domain.UserEntityDetails;
import com.web.flower.security.domain.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

// 시큐리티가 필터를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 가 있음.
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 안탄다.
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    private RefreshTokenRepository refreshTokenRepository;
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }


    // 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨
    // JWT 를 체크해 부합하면 권한을 부여한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("인증이나 권한이 필요한 주소 요청됨");
        String username = "";

        String jwtHeader= request.getHeader("Authorization");
        String refreshTokenHeader = request.getHeader("Refresh-Token");

        // header 유효성 검사
        if(jwtHeader==null || !jwtHeader.startsWith("Bearer")){
            chain.doFilter(request, response);
            return;
        }

        if(refreshTokenHeader!=null){ // 요청에서 리프레시 토큰을 가지고 왔다면
            String refreshToken = request.getHeader("Refresh-Token").replace("Bearer ","");
            try {
                username = validateToken("cos", refreshToken);
            }catch (TokenExpiredException e){
                System.out.println("RefreshToken 만료됨. 재인증이 필요합니다.");
                makeResponse(request, response, "refresh_token_expired", "refresh_token_expired. 재인증이 필요합니다.");
                chain.doFilter(request, response);
            }
            System.out.println("새 accessToken 발급");
            UserEntity userEntity = userRepository.findByUsername(username).get();

            UserEntityDetails userEntityDetails = new UserEntityDetails(userEntity);
            String newAccessToken = JwtService.createAccessToken(userEntity);

            response.addHeader(JwtProperties.JWT_HEADER,JwtProperties.TOKEN_PREFIX+newAccessToken);
            chain.doFilter(request, response);
        }

        // JWT를 검증해서 정상적인 사용자인지 확인!
        String jwtToken = request.getHeader("Authorization").replace("Bearer ","");

        try{
            username = validateToken("cos", jwtToken);
        }
        catch (TokenExpiredException e){
            // 토큰 기한 만료. refresh token 요청
            System.out.println("jwt 토큰 유효기간 만료. Refresh Token을 가져와주세요.");
            makeResponse(request, response, "auth_token_expired", "refresh Token 필요");
            chain.doFilter(request, response);
        }

        // 서명이 정상적으로 됨
        if(username!=null){
            UserEntity userEntity = userRepository.findByUsername(username).get();
            UserEntityDetails userEntityDetails = new UserEntityDetails(userEntity);

            // JWT 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
            // 강제로 시큐리티 세션에 접근하여 Authentication 객체를 저장
            setAuthenticationTokenToSecurityContext(userEntityDetails);
            chain.doFilter(request, response);
        }
    }

    private static void setAuthenticationTokenToSecurityContext(UserEntityDetails userEntityDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userEntityDetails, null, userEntityDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public String validateToken(String secret, String token) throws TokenExpiredException{
        String username = JWT.require(Algorithm.HMAC512(secret)).build()
                .verify(token)
                .getClaim("username").asString();
        return username;
    }

    private static void makeResponse(HttpServletRequest request, HttpServletResponse response, String msg, String memo) throws IOException, ServletException {
        ObjectMapper om = new ObjectMapper();
        Message message = Message.builder()
                .message(msg)
                .status(HttpStatus.UNAUTHORIZED)
                .memo(memo)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON.toString());
        om.writeValue(response.getOutputStream(),message);
    }

}
