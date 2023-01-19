package com.web.flower.domain.user.dto;

import com.web.flower.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class UserResListDto {

    private List<UserResDto> users = new ArrayList<>();

    public static UserResListDto toDto(List<User> userList){
        List<UserResDto> tmp = new ArrayList<>();
        userList.forEach(user -> tmp.add(UserResDto.toDto(user)));

        return UserResListDto.builder()
                .users(tmp)
                .build();
    }
}
