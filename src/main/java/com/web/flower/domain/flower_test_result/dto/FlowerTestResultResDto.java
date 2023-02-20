package com.web.flower.domain.flower_test_result.dto;

import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowerTestResultResDto {

    private UUID userId;
    private List<ResFlower> flowerList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResFlower{
        private int flowerNum;
        private LocalDateTime createdAt;
    }

    public static FlowerTestResultResDto toDto(List<FlowerTestResult> testResults){
        List<ResFlower> flowerList = new ArrayList<>();

        UUID id = testResults.get(0).getUser().getId();
        testResults.forEach(r -> flowerList.add(new ResFlower(r.getFlowerNum(), r.getCreatedAt())));

        return FlowerTestResultResDto.builder()
                .userId(id)
                .flowerList(flowerList)
                .build();
    }
}
