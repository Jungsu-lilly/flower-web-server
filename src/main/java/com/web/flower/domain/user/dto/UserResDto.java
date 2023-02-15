package com.web.flower.domain.user.dto;

import com.web.flower.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResDto {

    private UUID id;
    private String username;
    private LocalDateTime createdAt;

    public static UserResDto toDto(User userEntity){
        return UserResDto.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
