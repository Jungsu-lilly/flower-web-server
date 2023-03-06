package com.web.flower.domain.flower_select_result.controller;

import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultReqDto;
import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultResDto;
import com.web.flower.domain.flower_select_result.service.FlowerSelectResultService;
import com.web.flower.domain.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/flower-select-result")
@RequiredArgsConstructor
public class FlowerSelectResultController {

    private final FlowerSelectResultService flowerSelectResultService;

    @PostMapping("/one")
    public ResponseEntity<?> createOne(@RequestBody FlowerSelectResultReqDto.ReqCreate req){
        try {
            flowerSelectResultService.createOne(req);
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
            List<FlowerSelectResultResDto> resFlowerList = flowerSelectResultService.searchListByUser();
            Message message = Message.builder()
                    .data(resFlowerList)
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
    public ResponseEntity<?> deleteOne(@PathVariable("id")UUID id){
        Message message = new Message();
        try {
            flowerSelectResultService.deleteOne(id);
            message = Message.builder()
                    .status(HttpStatus.OK)
                    .message("success")
                    .build();
        } catch (Exception e) {
            message = Message.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("error")
                    .memo(e.getMessage())
                    .build();
        }
        return new ResponseEntity<>(message, message.getStatus());
    }
}
