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

    public void createOne(UserReqDto.ReqSignUp req) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(req.getUsername());
        if(byUsername.isPresent()){
            throw new Exception("이미 존재하는 이메일 입니다. 다른 이메일을 입력해주세요");
        }

        String password = passwordEncoder.encode(req.getPassword());

        User userEntity = User.builder()
                .id(UUID.randomUUID())
                .username(req.getUsername())
                .password(password)
                .profileName(req.getProfileName())
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(userEntity);
    }

    public UserResDto findById(UUID id) throws Exception {
        Optional<User> findUser = userRepository.findById(id);
        if(!findUser.isPresent()){
            throw new Exception("찾으려는 유저가 없습니다.");
        }
        return UserResDto.toDto(findUser.get());
    }

    public UserResListDto findAll(){
        List<User> allUserEntities = userRepository.findAll();
        return UserResListDto.toDto(allUserEntities);
    }

    public void deleteUser(UserReqDto.ReqDeleteOne req) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(req.getUsername());
        if(!byUsername.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인요망");
        }
        User user = byUsername.get();
        if(passwordEncoder.matches(req.getPassword(), user.getPassword())){
            userRepository.delete(user);
        }else{
            throw new Exception("username, password 가 일치하지 않습니다. 다시 한 번 확인해주세요.");
        }
    }

}
