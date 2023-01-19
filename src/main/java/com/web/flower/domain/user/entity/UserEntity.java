package com.web.flower.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @Type(type = "uuid-char")
    private UUID id;

    private String username;

    private String password;

    private String roles;

    private String profileName;

    private int profileAge;

    private String profileImagePath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public UserEntity(UUID id, String username, String password, String roles, String profileName, int profileAge, String profileImagePath, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.profileName = profileName;
        this.profileAge = profileAge;
        this.profileImagePath = profileImagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
