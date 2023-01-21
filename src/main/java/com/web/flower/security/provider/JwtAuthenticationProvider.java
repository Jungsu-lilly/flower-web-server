package com.web.flower.security.provider;

import com.web.flower.security.domain.UserEntityDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsService userDetailsService;

    public JwtAuthenticationProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println("=== FormAuthenticationProvider 실행 ===");
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        UserEntityDetails userEntityDetails = (UserEntityDetails) userDetailsService.loadUserByUsername(username);
        if(!passwordEncoder.matches(password, userEntityDetails.getPassword())){
            throw new BadCredentialsException("Invalid Password!");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userEntityDetails, null, userEntityDetails.getAuthorities());
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
