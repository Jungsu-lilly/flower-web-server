package com.web.flower.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static void makeCookie(String accessToken, HttpServletResponse response){
        Cookie resCookie = new Cookie("flower_token", accessToken);
        resCookie.setMaxAge(60*60*3); // 3시간
        resCookie.setPath("/");
        resCookie.setHttpOnly(true);
        response.addCookie(resCookie);
    }

}
