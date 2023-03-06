package com.web.flower.domain.refresh_token.controller;

import com.web.flower.security.auth.PrincipalDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/token")
public class RefreshTokenController {

    // user, manager, admin 접근 가능
    @GetMapping("/api/v1/user")
    public String user(Authentication authentication){
        PrincipalDetails userEntityDetails = (PrincipalDetails) authentication.getPrincipal();
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
