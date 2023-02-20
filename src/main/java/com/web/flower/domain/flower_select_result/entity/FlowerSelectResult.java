package com.web.flower.domain.flower_select_result.entity;

import com.web.flower.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Table(name = "flower_select")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlowerSelectResult {

    @Id
    @Type(type = "uuid-char")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int flowerNum;

    @Builder
    public FlowerSelectResult(UUID id, User user, int flowerNum) {
        this.id = id;
        this.user = user;
        this.flowerNum = flowerNum;
    }
}
