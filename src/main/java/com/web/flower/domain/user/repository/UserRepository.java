package com.web.flower.domain.user.repository;

import com.web.flower.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByProfileName(String profileName);

    Optional<UserEntity> findById(UUID id);

    Optional<UserEntity> findByUsername(String username);
}
