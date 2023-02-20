package com.web.flower.security.oauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.security.oauth.dto.LoginResponse;
import com.web.flower.security.oauth.service.OauthService;
import com.web.flower.domain.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequiredArgsConstructor
@Slf4j
public class OauthController {
    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OauthService oauthService;

    @GetMapping("/")
    public String home(){
        return "hi ";
    }

    @GetMapping("/login/oauth2/code/{provider}/{code}")
    public void oauthLogin(@PathVariable String provider, @PathVariable("code") String code,
                                        HttpServletResponse response) throws IOException {
        LoginResponse loginResponse = oauthService.login(provider, code);

        // data 지우기
        Message message = Message.builder()
                .data(loginResponse)
                .status(HttpStatus.OK)
                .message("oauth 로그인 완료. 엑세스 토큰 발급")
                .build();

        Cookie resCookie = new Cookie("Authorization", loginResponse.getAccessToken());
        resCookie.setMaxAge(600); // 600초
        resCookie.setHttpOnly(true);
        resCookie.setPath("/");
        response.addCookie(resCookie);

        ObjectMapper om = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        om.writeValue(response.getOutputStream(), message);
    }
}
