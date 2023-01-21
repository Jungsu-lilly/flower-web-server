package com.web.flower.domain.user.dto;

import com.web.flower.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class UserResListDto {

    private List<UserResDto> users = new ArrayList<>();

    public static UserResListDto toDto(List<UserEntity> userEntityList){
        List<UserResDto> tmp = new ArrayList<>();
        userEntityList.forEach(userEntity -> tmp.add(UserResDto.toDto(userEntity)));

        return UserResListDto.builder()
                .users(tmp)
                .build();
    }
}
