package com.web.flower.security.provider;

import com.web.flower.security.auth.UserEntityDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;

public class FormAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsService userDetailsService;

    public FormAuthenticationProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        System.out.println("username = " + username);
        String password = (String) authentication.getCredentials();
        System.out.println("password = " + password);

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
