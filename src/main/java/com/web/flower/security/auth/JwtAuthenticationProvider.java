package com.web.flower.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.transaction.Transactional;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final PrincipalUserDetailsService principalUserDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println("=== FormAuthenticationProvider 실행 ===");
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        PrincipalDetails principalDetails = (PrincipalDetails) principalUserDetailsService.loadUserByUsername(username);
        if(!passwordEncoder.matches(password, principalDetails.getPassword())){
            throw new BadCredentialsException("Invalid Password!");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
