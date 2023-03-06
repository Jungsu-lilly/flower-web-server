package com.web.flower.domain.flower_test_result.controller;

import com.web.flower.domain.flower_test_result.dto.FlowerTestResultReqDto;
import com.web.flower.domain.flower_test_result.dto.FlowerTestResultResDto;
import com.web.flower.domain.flower_test_result.service.FlowerTestResultService;
import com.web.flower.domain.message.Message;
import com.web.flower.domain.refresh_token.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/flower-test-result")
@RequiredArgsConstructor
public class FlowerTestResultController {

    private final FlowerTestResultService flowerTestResultService;
    private final JwtService jwtService;

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody FlowerTestResultReqDto.ReqCreate req){
        try {
            flowerTestResultService.createOne(req);
            Message message = Message.builder()
                    .status(HttpStatus.OK)
                    .message("success")
                    .build();
            return new ResponseEntity<>(message, message.getStatus());
        } catch (Exception e) {
            Message message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("error")
                    .memo(e.getMessage())
                    .build();
            return new ResponseEntity<>(message, message.getStatus());
        }
    }

    @GetMapping("/user/list")
    public ResponseEntity<?> searchListByUser(){
        try {
            List<FlowerTestResultResDto> flowerTestList = flowerTestResultService.searchListByUser();
            Message message = Message.builder()
                    .data(flowerTestList)
                    .status(HttpStatus.OK)
                    .message("success")
                    .build();
            return new ResponseEntity<>(message, message.getStatus());
        } catch (Exception e) {
            Message message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("error")
                    .memo(e.getMessage())
                    .build();
            return new ResponseEntity<>(message, message.getStatus());
        }
    }

    @DeleteMapping("/one/{id}")
    public ResponseEntity<?> deleteOne(@PathVariable("id") UUID flowerTestResultId){
        try {
            flowerTestResultService.deleteOne(flowerTestResultId);
            Message message = Message.builder()
                    .status(HttpStatus.OK)
                    .message("success")
                    .build();
            return new ResponseEntity<>(message, message.getStatus());

        } catch (Exception e) {
            Message message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(message, message.getStatus());
        }
    }

}
