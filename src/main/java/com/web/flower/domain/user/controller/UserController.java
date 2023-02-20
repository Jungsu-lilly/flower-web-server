package com.web.flower.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.jwt.service.JwtService;
import com.web.flower.domain.message.Message;
import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.dto.UserResDto;
import com.web.flower.domain.user.dto.UserResListDto;
import com.web.flower.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody UserReqDto.ReqSignUp req){
        System.out.println("--- 유저 생성 -----");
        Message message = new Message();
        try {
            userService.createOne(req);
            message = Message.builder()
                    .status(HttpStatus.OK)
                    .message("success")
                    .build();
        } catch (Exception e) {
            message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        }
        return new ResponseEntity<>(message, message.getStatus());
    }

    @DeleteMapping("/one")
    public ResponseEntity<?> deleteUser(@RequestBody UserReqDto.ReqDeleteOne req) {
        Message message = new Message();
        try {
            userService.deleteUser(req);
        } catch (Exception e) {
            message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(message, message.getStatus());
        }
        message = Message.builder()
                .status(HttpStatus.OK)
                .message("success")
                .build();

        return new ResponseEntity<>(message, message.getStatus());
    }

    @PostMapping("/logout")
    public void logOut(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Message message = new Message();

        Cookie cookie = Arrays.stream(request.getCookies())
                .filter(r -> r.getName().equals("Authorization"))
                .findAny()
                .orElse(null);

        String accessToken = cookie.getValue();

        try{
            jwtService.validateToken(accessToken);
        }
        catch (Exception e){
            throw new Exception("Invalid Username");
        }

        Cookie resCookie = new Cookie("Authorization", accessToken);
        resCookie.setMaxAge(0);
        resCookie.setHttpOnly(true);
        resCookie.setPath("/");
        response.addCookie(resCookie);

        message = Message.builder()
                .status(HttpStatus.OK)
                .message("success")
                .memo("로그아웃 되었습니다.")
                .build();

        ObjectMapper om = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        om.writeValue(response.getOutputStream(), message);
    }
}
