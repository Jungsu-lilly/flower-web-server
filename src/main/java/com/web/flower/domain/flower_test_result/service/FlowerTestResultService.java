package com.web.flower.domain.flower_test_result.service;

import com.web.flower.domain.flower_select_result.entity.FlowerSelectResult;
import com.web.flower.domain.flower_select_result.repository.FlowerSelectResultRepository;
import com.web.flower.domain.flower_test_result.dto.FlowerTestResultReqDto;
import com.web.flower.domain.flower_test_result.dto.FlowerTestResultResDto;
import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import com.web.flower.domain.flower_test_result.repository.FlowerTestResultRepository;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.utils.SecurityContextHolderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FlowerTestResultService {

    private final FlowerTestResultRepository flowerTestResultRepository;
    private final FlowerSelectResultRepository flowerSelectResultRepository;
    private final UserRepository userRepository;

    public FlowerTestResultResDto.ResCreateOne createOne(FlowerTestResultReqDto.ReqCreate req) throws Exception {
        String username = SecurityContextHolderUtils.getUsername();

        Optional<User> findUser = userRepository.findByUsername(username);
        if(!findUser.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인요망");
        }

        User user = findUser.get();
        int flowerNum = req.getFlowerNum();
        if(flowerNum<1 || flowerNum >31){
            throw new Exception("꽃 번호의 범위를 다시 한번 확인해주세요: (1~31)");
        }

        FlowerTestResult flowerTestResult = FlowerTestResult.builder()
                .id(UUID.randomUUID())
                .user(user)
                .flowerNum(req.getFlowerNum())
                .createdAt(LocalDateTime.now())
                .build();
        flowerTestResultRepository.save(flowerTestResult);

        boolean select = false;
        Optional<FlowerSelectResult> findFlowerSelectResult = flowerSelectResultRepository.findByUserAndFlowerNum(username, flowerNum);
        if(findFlowerSelectResult.isPresent()){ // 찜한 꽃이라면
            select = true;
        }
        FlowerTestResultResDto.ResCreateOne resTestResult = new FlowerTestResultResDto.ResCreateOne(flowerTestResult.getId(), flowerNum, select);
        return resTestResult;
    }

    public List<FlowerTestResultResDto> searchListByUser() throws Exception {
        String username = SecurityContextHolderUtils.getUsername();
        Optional<User> findUser = userRepository.findByUsername(username);
        if(!findUser.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }
        List<FlowerTestResult> flowerTestResults = flowerTestResultRepository.findByUsername(username);

        Comparator<FlowerTestResult> comparator = (f1, f2) -> Integer.valueOf(
                f2.getCreatedAt().compareTo(f1.getCreatedAt())
        );
        Collections.sort(flowerTestResults, comparator);
        List<FlowerTestResultResDto> flowerTestResultResDtoList = new ArrayList<>();

        flowerTestResults.forEach(ft -> {
            flowerTestResultResDtoList.add(FlowerTestResultResDto.toDto(ft));
        });
        return flowerTestResultResDtoList;
    }

    public void deleteOne(UUID flowerTestResultId) throws Exception {
        String username = SecurityContextHolderUtils.getUsername();

        Optional<User> findUser = userRepository.findByUsername(username);
        if(!findUser.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인 요망");
        }

        Optional<FlowerTestResult> findFlowerTestResult = flowerTestResultRepository.findById(flowerTestResultId);
        if(!findFlowerTestResult.isPresent()){
            throw new Exception("id를 다시 확인해주세요.");
        }
        FlowerTestResult flowerTestResult = findFlowerTestResult.get();
        flowerTestResultRepository.delete(flowerTestResult);
    }
}