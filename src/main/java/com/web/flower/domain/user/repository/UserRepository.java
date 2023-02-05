package com.web.flower.domain.user.repository;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<User> findByUsername(String username);
}
