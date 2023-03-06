package com.web.flower.domain.social_login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.message.Message;
import com.web.flower.domain.social_login.dto.LoginResponse;
import com.web.flower.domain.social_login.dto.SocialLoginReqDto;
import com.web.flower.domain.social_login.service.SocialLoginService;
import com.web.flower.utils.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequestMapping("/api/social-login")
@RequiredArgsConstructor
public class SocialLoginController {
    private final SocialLoginService socialLoginService;

    @PostMapping("")
    public void socialLogin(HttpServletResponse response, @RequestBody SocialLoginReqDto req) throws IOException {

        ObjectMapper om = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON.toString());

        LoginResponse loginResponse = null;
        try {
            loginResponse = socialLoginService.socialLogin(req);
        } catch (Exception e) {
            Message message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("error")
                    .memo(e.getMessage())
                    .build();
            om.writeValue(response.getOutputStream(), message);
            return;
        }
        Message message = Message.builder()
                .status(HttpStatus.OK)
                .message("success")
                .memo("소셜 로그인 성공. 엑세스 토큰을 발급힙니다.")
                .build();

        String accessToken = loginResponse.getAccessToken();
        CookieUtils.makeCookie(accessToken, response);

        om.writeValue(response.getOutputStream(), message);
    }
}
