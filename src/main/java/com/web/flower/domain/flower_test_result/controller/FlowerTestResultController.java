package com.web.flower.domain.flower_test_result.controller;

import com.web.flower.domain.flower_test_result.dto.FlowerTestResultReqDto;
import com.web.flower.domain.flower_test_result.dto.FlowerTestResultResDto;
import com.web.flower.domain.flower_test_result.service.FlowerTestResultService;
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
@RequestMapping("api/flower-test-result")
@RequiredArgsConstructor
public class FlowerTestResultController {

    private final FlowerTestResultService flowerTestResultService;
    private final JwtService jwtService;

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody FlowerTestResultReqDto.ReqCreate req, HttpServletRequest request){
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

            flowerTestResultService.createOne(req, username);
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

            FlowerTestResultResDto flowerList = flowerTestResultService.searchListByUser(username);
            message = Message.builder()
                    .data(flowerList)
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

            flowerTestResultService.deleteOne(username, flowerNum);
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
