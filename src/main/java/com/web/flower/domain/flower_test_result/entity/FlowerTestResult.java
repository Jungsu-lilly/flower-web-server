package com.web.flower.domain.flower_test_result.entity;

import com.web.flower.domain.user.entity.User;
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
@Table(name = "flower_test_result")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlowerTestResult {

    @Id
    @Type(type = "uuid-char")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int flowerNum;

    private LocalDateTime createdAt;

    @Builder
    public FlowerTestResult(UUID id, User user, int flowerNum, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.flowerNum = flowerNum;
        this.createdAt = createdAt;
    }
}
