package com.web.flower.security.config;

import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.filter.JwtAuthenticationFilter;
import com.web.flower.security.filter.JwtAuthorizationFilter;
import com.web.flower.domain.jwt.repository.RefreshTokenRepository;
import com.web.flower.domain.jwt.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true) // 특정 주소 접근시 권한 및 인증을 위한 어노테이션 활성화
public class SecurityConfig {

    @Autowired private CorsFilter corsFilter;

    @Autowired private UserRepository userRepository;

    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Autowired private JwtService jwtService;

    @Autowired
    private AuthenticationSuccessHandler customAuthenticationSuccessHandler;

//    @Autowired
//    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                // 세션을 사용하지 않겠다. -> 세션 stateless 서버로 만들겠다.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .apply(new MyCustomDsl())
                .and()
                .authorizeRequests()
                .antMatchers("/api/v1/user/**")
                .access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")

                .antMatchers("/api/v1/admin/**")
                .access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll();
//
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .loginProcessingUrl("/loginProc")
//                .defaultSuccessUrl("/")
//                .and()
//                .oauth2Login();
//                .successHandler(oAuth2SuccessHandler);
//                .loginPage("/login");

        return http.build();  // SecurityFilterChain 생성
    }

    public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
            http
                    .addFilter(corsFilter)
                    .addFilter(new JwtAuthenticationFilter(authenticationManager, refreshTokenRepository, jwtService))
                    .addFilter(new JwtAuthorizationFilter(authenticationManager, userRepository, refreshTokenRepository, jwtService));
        }
    }

}
