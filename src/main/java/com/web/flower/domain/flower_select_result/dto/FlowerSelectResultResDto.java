package com.web.flower.domain.flower_select_result.dto;

import com.web.flower.domain.flower_select_result.entity.FlowerSelectResult;
import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowerSelectResultResDto {

    private UUID userId;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResFlowerList{
        private UUID userId;
        private List<Integer> flowerNumbers;
    }

    public static FlowerSelectResultResDto.ResFlowerList toResFlowerList(List<FlowerSelectResult> testResults){
        List<Integer> flowerNums = new ArrayList<>();

        UUID id = testResults.get(0).getUser().getId();
        testResults.forEach(r -> flowerNums.add(r.getFlowerNum()));

        return FlowerSelectResultResDto.ResFlowerList.builder()
                .userId(id)
                .flowerNumbers(flowerNums)
                .build();
    }
}
