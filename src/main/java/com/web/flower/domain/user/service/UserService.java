package com.web.flower.domain.user.service;

import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.dto.UserResDto;
import com.web.flower.domain.user.dto.UserResListDto;
import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
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

    public String save(UserReqDto reqDto) throws Exception {
        Optional<UserEntity> findUser = userRepository.findByUsername(reqDto.getUsername());

        if(findUser.isPresent()){
            throw new Exception("유저 이름(이메일)이 이미 존재합니다. 다른 값을 입력해주세요.");
        }
        UUID id = UUID.randomUUID();
        String password = passwordEncoder.encode(reqDto.getPassword());

        UserEntity userEntity = UserEntity.builder()
                .id(id)
                .username(reqDto.getUsername())
                .password(password)
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();

        try{
            userRepository.save(userEntity);
        }
        catch (Exception e){
            return "FAIL";
        }
        return "SUCCESS";
    }

    public UserResDto findById(UUID id){
        Optional<UserEntity> findUser = userRepository.findById(id);
        if(!findUser.isPresent()){
            throw new NoSuchElementException("찾으려는 유저가 없습니다.");
        }
        return UserResDto.toDto(findUser.get());
    }

    public UserResListDto findAll(){
        List<UserEntity> allUserEntities = userRepository.findAll();
        return UserResListDto.toDto(allUserEntities);
    }

    public String deleteUser(UserReqDto reqDto) throws Exception {
        String username = reqDto.getUsername();
        Optional<UserEntity> findUser = userRepository.findByUsername(reqDto.getUsername());
        if (!findUser.isPresent()) {
            throw new Exception("찾으려는 유저가 없습니다.");
        }

        UserEntity userEntity = findUser.get();
        if(!passwordEncoder.matches(reqDto.getPassword(), userEntity.getPassword())){
            throw new Exception("비밀번호가 맞지 않습니다.");
        }

        userRepository.delete(userEntity);
        return "SUCCESS";
    }

}