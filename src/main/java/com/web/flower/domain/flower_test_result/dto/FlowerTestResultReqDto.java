package com.web.flower.domain.flower_test_result.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowerTestResultReqDto {

    private UUID userId;
    private int flowerNum;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReqCreate{
//        private UUID userId;
        private int flowerNum;
    }
}
