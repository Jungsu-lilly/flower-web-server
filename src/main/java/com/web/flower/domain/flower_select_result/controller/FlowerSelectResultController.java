package com.web.flower.domain.flower_select_result.controller;

import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultReqDto;
import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultResDto;
import com.web.flower.domain.flower_select_result.service.FlowerSelectResultService;
import com.web.flower.domain.jwt.service.JwtService;
import com.web.flower.domain.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
@RequestMapping("api/flower-select-result")
@RequiredArgsConstructor
public class FlowerSelectResultController {

    private final FlowerSelectResultService flowerSelectResultService;
    private final JwtService jwtService;

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody FlowerSelectResultReqDto.ReqCreate req, HttpServletRequest request){
        System.out.println("flower-select : createOne 메서드");
        Message message = new Message();
        try {
            Cookie cookie = Arrays.stream(request.getCookies())
                    .filter(r -> r.getName().equals("Authorization"))
                    .findAny()
                    .orElse(null);
            if(cookie==null) {
                message = Message.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("No jwt in Cookie")
                        .build();
                return new ResponseEntity<>(message, message.getStatus());
            }
            String username = jwtService.validateToken(cookie.getValue());
            System.out.println("username = " + username);

            flowerSelectResultService.createOne(req, username);
            message = Message.builder()
                    .status(HttpStatus.OK)
                    .message("success")
                    .build();
        } catch (Exception e) {
            System.out.println("wwww");
            message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
        }
        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/user/list")
    public ResponseEntity<?> searchListByUser(HttpServletRequest request){
        Message message = new Message();
        try {
            Cookie cookie = Arrays.stream(request.getCookies())
                    .filter(r -> r.getName().equals("Authorization"))
                    .findAny()
                    .orElse(null);
            if(cookie==null) {
                message = Message.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("No jwt in Cookie")
                        .build();
                return new ResponseEntity<>(message, message.getStatus());
            }
            String username = jwtService.validateToken(cookie.getValue());

            FlowerSelectResultResDto.ResFlowerList resFlowerList = flowerSelectResultService.searchListByUser(username);
            message = Message.builder()
                    .data(resFlowerList)
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

    @DeleteMapping("/one/{flowerNum}")
    public ResponseEntity<?> deleteOne(@PathVariable("flowerNum") int flowerNum, HttpServletRequest request){
        Message message = new Message();
        try {
            Cookie cookie = Arrays.stream(request.getCookies())
                    .filter(r -> r.getName().equals("Authorization"))
                    .findAny()
                    .orElse(null);
            if(cookie==null) {
                message = Message.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("No jwt in Cookie")
                        .build();
                return new ResponseEntity<>(message, message.getStatus());
            }
            String username = jwtService.validateToken(cookie.getValue());

            flowerSelectResultService.deleteOne(username, flowerNum);
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
}
