package com.web.flower.security.config.auth;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("userDetailsService")
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("=== PrincipalUserDetailsService : loadUserByUsername() =========");
        Optional<User> byUsername = userRepository.findByUsername(username);
//        List<GrantedAuthority> roles = new ArrayList<>();
//        roles.add(new SimpleGrantedAuthority(user.getRole()));

        User user = byUsername.get();
        System.out.println("user.getUsername() = " + user.getUsername());
        return new PrincipalDetails(user);
    }
}
