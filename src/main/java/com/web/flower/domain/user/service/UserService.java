package com.web.flower.domain.user.service;

import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.dto.UserResDto;
import com.web.flower.domain.user.dto.UserResListDto;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public String save(UserReqDto reqDto){
        UUID id = UUID.randomUUID();
        String password = reqDto.getPassword() + UUID.randomUUID().toString();

        User user = User.builder()
                .id(id)
                .username(reqDto.getUsername())
                .password(password)
                .roles("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();

        try{
            userRepository.save(user);
        }
        catch (Exception e){
            return "FAIL";
        }
        return "SUCCESS";
    }

    public UserResDto findById(UUID id){
        Optional<User> findUser = userRepository.findById(id);
        if(!findUser.isPresent()){
            throw new NoSuchElementException("찾으려는 유저가 없습니다.");
        }
        return UserResDto.toDto(findUser.get());
    }

    public UserResListDto findAll(){
        List<User> allUsers = userRepository.findAll();
        return UserResListDto.toDto(allUsers);
    }

    public String deleteUser(UserReqDto reqDto) throws Exception {
        String username = reqDto.getUsername();
        Optional<User> findUser = userRepository.findByUsername(reqDto.getUsername());
        if (!findUser.isPresent()) {
            throw new NoSuchElementException("찾으려는 유저가 없습니다.");
        }

        User user = findUser.get();
        String password = user.getPassword();
        if(!reqDto.getPassword().equals(password)){
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
        return "SUCCESS";
    }

}
