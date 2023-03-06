package com.web.flower.domain.refresh_token.repository;

import com.web.flower.domain.refresh_token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findById(UUID id);

    Optional<RefreshToken> findByUserId(@Param("userId") UUID userId);

}
