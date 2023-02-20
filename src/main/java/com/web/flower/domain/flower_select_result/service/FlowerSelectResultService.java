package com.web.flower.domain.flower_select_result.service;

import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultReqDto;
import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultResDto;
import com.web.flower.domain.flower_select_result.entity.FlowerSelectResult;
import com.web.flower.domain.flower_select_result.repository.FlowerSelectResultRepository;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlowerSelectResultService {

    private final FlowerSelectResultRepository flowerSelectResultRepository;
    private final UserRepository userRepository;

    public void createOne(FlowerSelectResultReqDto.ReqCreate req, String username) throws Exception {

        Optional<User> byId = userRepository.findByUsername(username);
        if(!byId.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인요망");
        }
        User user = byId.get();
        int flowerNum = req.getFlowerNum();
        if(flowerNum<1 || flowerNum > 31){
            throw new Exception("꽃 번호의 범위를 다시 한번 확인해주세요: (1~31)");
        }
        Optional<FlowerSelectResult> byUserAndFlowerNum = flowerSelectResultRepository.findByUserAndFlowerNum(username, flowerNum);
        if(byUserAndFlowerNum.isPresent()){
            throw new Exception("이미 찜한 꽃입니다. 다른 값을 입력해주세요");
        }

        FlowerSelectResult flowerSelectResult = FlowerSelectResult.builder()
                .id(UUID.randomUUID())
                .user(user)
                .flowerNum(req.getFlowerNum())
                .build();
        flowerSelectResultRepository.save(flowerSelectResult);
    }

    public FlowerSelectResultResDto.ResFlowerList searchListByUser(String username) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if(!byUsername.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }
        User user = byUsername.get();
        List<FlowerSelectResult> flowerSelectResults = flowerSelectResultRepository.findByUsername(username);

        FlowerSelectResultResDto.ResFlowerList resFlowerList = FlowerSelectResultResDto.toResFlowerList(flowerSelectResults);
        return resFlowerList;
    }

    public void deleteOne(String username, int flowerNum) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if(!byUsername.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }
        User user = byUsername.get();

        Optional<FlowerSelectResult> byUserAndFlowerNum = flowerSelectResultRepository.findByUserAndFlowerNum(username, flowerNum);
        if(!byUserAndFlowerNum.isPresent()){
            throw new Exception("유저아이디와 꽃 번호를 다시 한번 확인해주세요.");
        }
        FlowerSelectResult flowerSelectResult = byUserAndFlowerNum.get();
        flowerSelectResultRepository.delete(flowerSelectResult);
    }
}
