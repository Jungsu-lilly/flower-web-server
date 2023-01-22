package com.web.flower.security;

public interface JwtProperties {

    int SEC = 1000;

    int MIN = 1000*60;
    int DAY = 1000*60*1440;

    String SECRET = "cos"; // 서버만 알고 있는 비밀값
    int EXPIRATION_TIME = MIN*7; // 7분

    int REFRESH_EXPIRATION_TIME = DAY*2; // 2일

    public String JWT_HEADER = "Authorization";
}
