package com.web.flower.domain.user.controller;

import com.web.flower.domain.message.entity.Message;
import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.dto.UserResDto;
import com.web.flower.domain.user.dto.UserResListDto;
import com.web.flower.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody UserReqDto req){
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

    @GetMapping("/all")
    public ResponseEntity<?> searchAll(){
        UserResListDto all = userService.findAll();
        Message message = Message.builder()
                .data(all)
                .status(HttpStatus.OK)
                .message("SUCCESS")
                .build();

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("one/id")
    public ResponseEntity<?> searchOne(@RequestParam UUID id){
        UserResDto byId = userService.findById(id);
        Message message = Message.builder()
                .data(byId)
                .status(HttpStatus.OK)
                .message("SUCCESS")
                .build();

        return new ResponseEntity<>(message, message.getStatus());
    }

    @DeleteMapping("/one")
    public ResponseEntity<?> deleteUser(@RequestBody UserReqDto req) throws Exception {
        try {
            userService.deleteUser(req);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        Message message = Message.builder()
                .status(HttpStatus.OK)
                .message("SUCCESS")
                .build();

        return new ResponseEntity<>(message, message.getStatus());
    }
}
