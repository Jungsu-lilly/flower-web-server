package com.web.flower.security.service;

import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.auth.UserEntityDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if(!byUsername.isPresent()){
            throw new UsernameNotFoundException("해당 유저 이름(이메일)이 존재하지 않습니다.");
        }
        UserEntity userEntity = byUsername.get();
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(userEntity.getRole()));

        UserEntityDetails userEntityDetails = new UserEntityDetails(userEntity, roles);
        return userEntityDetails;
    }
}
