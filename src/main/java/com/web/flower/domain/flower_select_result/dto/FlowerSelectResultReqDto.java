package com.web.flower.domain.flower_select_result.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlowerSelectResultReqDto {

    private int flowerNum;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReqCreate{
        private int flowerNum;
    }
}
