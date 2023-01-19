package com.web.flower.security.auth;

import com.web.flower.domain.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UserEntityDetails extends User {

    private final UserEntity userEntity;

    public UserEntityDetails(UserEntity userEntity, Collection<? extends GrantedAuthority> authorities) {
        super(userEntity.getUsername(), userEntity.getPassword(), authorities);
        this.userEntity = userEntity;
    }

    public UserEntity getUserEntity(){
        return userEntity;
    }

}
