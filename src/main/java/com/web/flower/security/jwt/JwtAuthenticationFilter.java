package com.web.flower.security.jwt;

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

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter: 로그인 요청 도착");

        try {
            ObjectMapper om = new ObjectMapper(); // JSON 데이터도 파싱해줄 수 있음.
            UserEntity userEntity = om.readValue(request.getInputStream(), UserEntity.class);
            System.out.println("요청된 유저 정보 = " + userEntity);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getPassword());

            // CustomUserDetailsService 의 loadUserByUsername() 함수 실행 - 인증객체에 사용 정보 담김
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            UserEntityDetails userDetails = (UserEntityDetails) authentication.getPrincipal();
            System.out.println("로그인 완료됨.\nuserDetails = " + userDetails + "\n");

            return authentication;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // attemptAuthentication 실행 후, 인증이 정상적으로 되었다면 successfulAuthentication 함수가 실행된다.
    // JWt 토큰을 만들어 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication 실행됨");
        UserEntityDetails userEntityDetails = (UserEntityDetails) authResult.getPrincipal();

        // Hash 암호 방식
        String jwtToken = JWT.create()
                .withSubject("꽃물Token") // 토큰이름
                .withExpiresAt(new Date(System.currentTimeMillis()+1000*60*7)) // 유효시간: 7분
                .withClaim("id", userEntityDetails.getUserEntity().getId().toString())
                .withClaim("username", userEntityDetails.getUserEntity().getUsername())
                .sign(Algorithm.HMAC512("cos"));

        System.out.println("jwtToken = " + jwtToken);
        response.addHeader("Authorization","Bearer "+jwtToken);
    }

}
