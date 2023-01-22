package com.web.flower.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
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
import com.web.flower.security.validate.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;
import java.util.UUID;

// 시큐리티가 필터를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 가 있음.
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 안탄다.
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    private RefreshTokenRepository refreshTokenRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    // 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨
    // JWT 를 체크해 부합하면 권한을 부여한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("인증이나 권한이 필요한 주소 요청됨");
        String username = null;

        String accessToken = request.getHeader("Authorization");

        if(accessToken ==null) {
            chain.doFilter(request, response);
            return;
        }

        try{
            username = TokenValidator.validateToken("cos", accessToken);
        }
        catch (TokenExpiredException e){
            System.out.println("=== AccessToken 유효기간 만료 ===");

            String userId = TokenValidator.getUserIdFromToken(accessToken);
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(UUID.fromString(userId));
            if(!refreshToken.isPresent()){
                throw new NullPointerException("no refresh Token");
            }
            try{
                TokenValidator.validateToken("cos", refreshToken.get().getValue());
            }
            catch (TokenExpiredException e1){
                makeResponse(request, response,"refresh_token_expired", "재인증 필요");
                chain.doFilter(request, response);
            }

            UserEntity userEntity = userRepository.findById(UUID.fromString(userId)).get();
            String newAccessToken = JwtService.createAccessToken(userEntity);

            makeResponse(request, response, "access_token_expired", "AccessToken을 재발급합니다.");
            System.out.println("새 accessToken 발급");
            String jwtToken = JwtService.createAccessToken(userEntity);

            Cookie cookie = new Cookie("Authorization",jwtToken);
            cookie.setMaxAge(60*30); // 30분
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            response.addHeader(JwtProperties.JWT_HEADER, "Bearer "+jwtToken);
            setAuthenticationTokenToSecurityContext(userEntity);
            chain.doFilter(request, response);
        }
        catch (SignatureVerificationException e){
            System.out.println("Access 토큰값이 유효하지 않습니다. 다시 입력해주세요.");
            makeResponse(request, response, "wrong_token_value", "Access 토큰값이 잘못되었습니다.");
            chain.doFilter(request, response);
        }

        // 서명이 정상적으로 됨
        if(username!=null){
            UserEntity userEntity = userRepository.findByUsername(username).get();
            // JWT 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
            // 강제로 시큐리티 세션에 접근하여 Authentication 객체를 저장
            setAuthenticationTokenToSecurityContext(userEntity);
            chain.doFilter(request, response);
        }
    }

    private static void setAuthenticationTokenToSecurityContext(UserEntity userEntity) {
        UserEntityDetails userEntityDetails = new UserEntityDetails(userEntity);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userEntityDetails, null, userEntityDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
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
