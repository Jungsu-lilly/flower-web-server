package com.web.flower.security.service;

import com.web.flower.security.domain.RefreshToken;
import com.web.flower.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(String refreshToken){
        UUID id = UUID.randomUUID();

        RefreshToken token = RefreshToken.builder()
                .id(id)
                .refreshToken(refreshToken).build();
        refreshTokenRepository.save(token);
    }

    public void delete(UUID id){
        refreshTokenRepository.deleteById(id);
    }


}
