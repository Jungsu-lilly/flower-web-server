package com.web.flower.utils;

import com.web.flower.security.auth.PrincipalDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityContextHolderUtils {

    public static UUID getUserId(){
        PrincipalDetails principal = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = principal.getUser().getId();
        return userId;
    }

    public static String getUsername(){
        PrincipalDetails principal = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = principal.getUser().getUsername();
        return username;
    }

}
