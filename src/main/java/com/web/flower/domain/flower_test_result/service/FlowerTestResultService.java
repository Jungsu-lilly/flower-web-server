package com.web.flower.domain.flower_test_result.service;

import com.web.flower.domain.flower_test_result.dto.FlowerTestResultReqDto;
import com.web.flower.domain.flower_test_result.dto.FlowerTestResultResDto;
import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import com.web.flower.domain.flower_test_result.repository.FlowerTestResultRepository;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlowerTestResultService {

    private final FlowerTestResultRepository flowerTestResultRepository;
    private final UserRepository userRepository;

    public void createOne(FlowerTestResultReqDto.ReqCreate req, String username) throws Exception {

        Optional<User> byId = userRepository.findByUsername(username);
        if(!byId.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인요망");
        }
        User user = byId.get();
        int flowerNum = req.getFlowerNum();
        if(flowerNum<1 || flowerNum >31){
            throw new Exception("꽃 번호의 범위를 다시 한번 확인해주세요: (1~31)");
        }
        Optional<FlowerTestResult> byUserAndFlowerNum = flowerTestResultRepository.findByUserAndFlowerNum(username, flowerNum);
        if(byUserAndFlowerNum.isPresent()){
            FlowerTestResult flowerTestResult = byUserAndFlowerNum.get();
            flowerTestResultRepository.delete(flowerTestResult);
        }
        FlowerTestResult flowerTestResult = FlowerTestResult.builder()
                .id(UUID.randomUUID())
                .user(user)
                .flowerNum(req.getFlowerNum())
                .createdAt(LocalDateTime.now())
                .build();

        flowerTestResultRepository.save(flowerTestResult);
    }

    public FlowerTestResultResDto searchListByUser(String username) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if(!byUsername.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }
        User user = byUsername.get();
        List<FlowerTestResult> flowerTestResults = flowerTestResultRepository.findByUsername(username);

        FlowerTestResultResDto flowerList = FlowerTestResultResDto.toDto(flowerTestResults);
        return flowerList;
    }

    public void deleteOne(String username, int flowerNum) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if(!byUsername.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }
        User user = byUsername.get();

        Optional<FlowerTestResult> byUserAndFlowerNum = flowerTestResultRepository.findByUserAndFlowerNum(username, flowerNum);
        if(!byUserAndFlowerNum.isPresent()){
            throw new Exception("유저아이디와 꽃 번호를 다시 한번 확인해주세요.");
        }
        FlowerTestResult flowerTestResult = byUserAndFlowerNum.get();
        flowerTestResultRepository.delete(flowerTestResult);
    }
}