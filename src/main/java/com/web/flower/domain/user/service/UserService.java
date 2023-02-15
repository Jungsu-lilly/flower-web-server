package com.web.flower.domain.user.service;

import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.dto.UserResDto;
import com.web.flower.domain.user.dto.UserResListDto;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsService userDetailsService;

    public void createOne(UserReqDto reqDto) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(reqDto.getUsername());
        if(byUsername.isPresent()){
            throw new Exception("이미 존재하는 이메일 입니다. 다른 이메일을 입력해주세요");
        }

        String password = passwordEncoder.encode(reqDto.getPassword());
        String userRole = reqDto.getRole();
        if(userRole == null){
            userRole = "ROLE_USER";
        }

        User userEntity = User.builder()
                .id(UUID.randomUUID())
                .username(reqDto.getUsername())
                .password(password)
                .role(userRole)
                .profileName(reqDto.getProfileName())
                .profileAge(reqDto.getProfileAge())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(userEntity);
    }

    public UserResDto findById(UUID id){
        Optional<User> findUser = userRepository.findById(id);
        if(!findUser.isPresent()){
            throw new NoSuchElementException("찾으려는 유저가 없습니다.");
        }
        return UserResDto.toDto(findUser.get());
    }

    public UserResListDto findAll(){
        List<User> allUserEntities = userRepository.findAll();
        return UserResListDto.toDto(allUserEntities);
    }

    public void deleteUser(UserReqDto reqDto) throws Exception {
        String username = reqDto.getUsername();
        Optional<User> byUsername = userRepository.findByUsername(reqDto.getUsername());
        User user = byUsername.get();

        if(!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())){
            throw new Exception("비밀번호가 맞지 않습니다.");
        }
        userRepository.delete(user);
    }

}
