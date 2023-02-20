package com.web.flower.domain.user.entity;

import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Type(type = "uuid-char")
    private UUID id;
    private String username;
    private String password;
    private String provider;
    private String providerId;
    private String role;
    private String profileName;
    private String profileImagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<FlowerTestResult> flowerTestResults = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<com.web.flower.domain.flower_select_result.entity.FlowerSelectResult> flowerSelectResults = new ArrayList<>();

    @Builder
    public User(UUID id, String username, String password, String provider, String providerId, String profileName, String role, String profileImagePath, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.profileName = profileName;
        this.profileImagePath = profileImagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void setPassword(String password){this.password = password;}

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
