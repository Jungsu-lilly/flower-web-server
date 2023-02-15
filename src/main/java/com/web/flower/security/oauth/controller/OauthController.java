package com.web.flower.security.oauth.controller;

import com.web.flower.security.oauth.dto.LoginResponse;
import com.web.flower.security.oauth.service.OauthService;
import com.web.flower.domain.message.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/login/oauth2/code/{provider}")
    public ResponseEntity<?> oauthLogin(@PathVariable String provider, @RequestParam String code){
        System.out.println("provider = " + provider);
        System.out.println("code = " + code);

        LoginResponse loginResponse = oauthService.login(provider, code);

        // data 지우기
        Message message = Message.builder()
                .data(loginResponse)
                .status(HttpStatus.OK)
                .message("oauth 로그인 완료. 엑세스 토큰 발급")
                .build();

        // 쿠키에 내보내면 됨 엑세스 토큰만!
        return new ResponseEntity<>(message, message.getStatus());
    }
}
