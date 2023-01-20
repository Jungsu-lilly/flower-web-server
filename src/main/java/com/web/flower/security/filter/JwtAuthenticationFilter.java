package com.web.flower.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.security.auth.UserEntityDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

// 스프링 시큐리티에서 UsernamePasswordAuthenticationFilter 가 있다.
// /login 요청해서 username, password POST 전송하면 위 필터가 동작한다.
// 현재는 formLogin disable 했기 때문에 작동하지 않음.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    // /login 요청 시 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter: 로그인 시도중");

        // 1. username, password를 받는다.
        try {
            ObjectMapper om = new ObjectMapper(); // JSON 데이터도 파싱해줄 수 있음.
            UserEntity userEntity = om.readValue(request.getInputStream(), UserEntity.class);
            System.out.println("요청된 유저 정보 = " + userEntity);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getPassword());

            // CustomUserDetailsService 의 loadUserByUsername() 함수 실행 - 인증객체에 사용 정보 담김
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // authentication 객체가 session 영역에 저장을 해야하고 그 방법이 return 해주면 됨.
            // 리턴의 이유는 권한 관리를 security가 대신 해주기 때문에 편하려고 하는 것임.
            // 굳이 JWT 토큰을 사용하면서 세션을 만들 이유가 없음. 단지 권한 처리 떄문에 session에 넣어준다.
            UserEntityDetails userDetails = (UserEntityDetails) authentication.getPrincipal();
            System.out.println("로그인 완료됨. userDetails = " + userDetails);

            return authentication;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // attemptAuthentication실행 후 인증이 정상적으로 되었다면 successfulAuthentication 함수가 실행된다.
    // JWt 토큰을 만들어 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication 실행됨 : 인증이 완료되었다는 뜻");
        UserEntityDetails userEntityDetails = (UserEntityDetails) authResult.getPrincipal();

        // Hash 암호 방식
        String jwtToken = JWT.create()
                .withSubject("cos-token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+1000*60*7)) // 유효시간: 7분
                .withClaim("id", userEntityDetails.getUserEntity().getId().toString())
                .withClaim("username", userEntityDetails.getUserEntity().getUsername())
                .sign(Algorithm.HMAC512("cos"));

        System.out.println("jwtToken = " + jwtToken);
        response.addHeader("Authorization","Bearer "+jwtToken);
    }

}
