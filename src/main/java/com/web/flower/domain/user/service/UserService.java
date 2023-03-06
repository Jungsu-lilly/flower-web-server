package com.web.flower.domain.user.service;

import com.web.flower.domain.refresh_token.entity.RefreshToken;
import com.web.flower.domain.refresh_token.repository.RefreshTokenRepository;
import com.web.flower.domain.user.dto.UserReqDto;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.utils.SecurityContextHolderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenRepository refreshTokenRepository;

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

    public void deleteUser(UserReqDto.ReqDeleteOne req) throws Exception {
        Optional<User> byUsername = userRepository.findByUsername(req.getUsername());
        if(!byUsername.isPresent()){
            throw new Exception("유저가 존재하지 않습니다. userId 확인요망");
        }
        User user = byUsername.get();
        if(passwordEncoder.matches(req.getPassword(), user.getPassword())){
            userRepository.delete(user);
            Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findByUserId(user.getId());
            if(findRefreshToken.isPresent()){
                refreshTokenRepository.delete(findRefreshToken.get());
            }
        }else{
            throw new Exception("username, password 가 일치하지 않습니다. 다시 한 번 확인해주세요.");
        }
    }

    public void logOut() {
        UUID userId = SecurityContextHolderUtils.getUserId();
        Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findByUserId(userId);

        if(findRefreshToken.isPresent()){
            refreshTokenRepository.delete(findRefreshToken.get());
        }

    }
}
