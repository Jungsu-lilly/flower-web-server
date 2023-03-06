package com.web.flower.domain.flower_test_result.dto;

import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowerTestResultResDto {

    private UUID id;
    private int flowerNum;
    private LocalDateTime createdAt;

    public static FlowerTestResultResDto toDto(FlowerTestResult flowerTestResult) {
        return new FlowerTestResultResDto(flowerTestResult.getId(),flowerTestResult.getFlowerNum(), flowerTestResult.getCreatedAt());
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResCreateOne{
        private UUID id;
        private int flowerNum;
        private boolean select;
    }
}
