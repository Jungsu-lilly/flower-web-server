package com.web.flower.security.controller;

import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.security.auth.UserEntityDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @GetMapping("home")
    public String home(){
        return "home";
    }

    @PostMapping("token")
    public String token(){
        return "token";
    }

    // user, manager, admin 접근 가능
    @GetMapping("/api/v1/user")
    public String user(Authentication authentication){
        UserEntityDetails userEntityDetails = (UserEntityDetails) authentication.getPrincipal();
        System.out.println("authentication = " + userEntityDetails.getUsername());
        return "user";
    }

    // manager, admin 접근 가능
    @GetMapping("/api/v1/manager")
    public String manager(){
        return "manager";
    }

    // admin 접근 가능
    @GetMapping("/api/v1/admin")
    public String admin(){
        return "admin";
    }

}
