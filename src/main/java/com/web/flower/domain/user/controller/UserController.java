package com.web.flower.domain.user.controller;

import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.dto.UserResDto;
import com.web.flower.domain.user.dto.UserResListDto;
import com.web.flower.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public String createUser(@RequestBody UserReqDto req){
        return userService.save(req);
    }

    @GetMapping("")
    public UserResListDto findAll(){
        return userService.findAll();
    }

    @GetMapping("/id")
    public UserResDto findById(@RequestParam UUID id){
        return userService.findById(id);
    }

    @DeleteMapping("")
    public String deleteUser(@RequestBody UserReqDto req){
        try {
            return userService.deleteUser(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
