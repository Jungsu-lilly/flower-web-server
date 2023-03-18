package com.web.flower.domain.flower_select_result.dto;

import com.web.flower.domain.flower_select_result.entity.FlowerSelectResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowerSelectResultResDto {

    private int flowerNum;
    private LocalDateTime createdAt;

    public static List<FlowerSelectResultResDto> toDto(List<FlowerSelectResult> flowerSelectResults) {
        List<FlowerSelectResultResDto> list = new ArrayList<>();
        flowerSelectResults.forEach(f -> {
            list.add(new FlowerSelectResultResDto(f.getFlowerNum(), f.getCreatedAt()));
        });
        return list;
    }
}
