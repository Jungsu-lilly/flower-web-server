package com.web.flower.domain.flower_select_result.service;

import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultReqDto;
import com.web.flower.domain.flower_select_result.dto.FlowerSelectResultResDto;
import com.web.flower.domain.flower_select_result.entity.FlowerSelectResult;
import com.web.flower.domain.flower_select_result.repository.FlowerSelectResultRepository;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.utils.SecurityContextHolderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FlowerSelectResultService {

    private final FlowerSelectResultRepository flowerSelectResultRepository;
    private final UserRepository userRepository;

    public void createOne(FlowerSelectResultReqDto.ReqCreate req) throws Exception {
        String username = SecurityContextHolderUtils.getUsername();
        
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
                .createdAt(LocalDateTime.now())
                .build();
        flowerSelectResultRepository.save(flowerSelectResult);
    }

    public List<FlowerSelectResultResDto> searchListByUser() throws Exception {
        String username = SecurityContextHolderUtils.getUsername();

        Optional<User> findUser = userRepository.findByUsername(username);
        if(!findUser.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }
        List<FlowerSelectResult> flowerSelectResults = flowerSelectResultRepository.findByUsername(username);

        Comparator<FlowerSelectResult> comparator = (f1, f2) -> Integer.valueOf(
                f2.getCreatedAt().compareTo(f1.getCreatedAt())
        );
        Collections.sort(flowerSelectResults, comparator);
        List<FlowerSelectResultResDto> resFlowerList = FlowerSelectResultResDto.toDto(flowerSelectResults);

        return resFlowerList;
    }

    public void deleteOne(int flowerNum) throws Exception {
        String username = SecurityContextHolderUtils.getUsername();
        Optional<User> findUser = userRepository.findByUsername(username);
        if(!findUser.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }

        Optional<FlowerSelectResult> findFlowerSelectResult = flowerSelectResultRepository.findByUserAndFlowerNum(username, flowerNum);
        if(!findFlowerSelectResult.isPresent()){
            throw new Exception("유저아이디와 꽃 번호를 다시 한번 확인해주세요.");
        }
        FlowerSelectResult flowerSelectResult = findFlowerSelectResult.get();
        flowerSelectResultRepository.delete(flowerSelectResult);
    }
}
