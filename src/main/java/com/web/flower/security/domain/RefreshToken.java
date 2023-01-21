package com.web.flower.security.domain;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Builder
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshToken {

    @Id
    @Type(type = "uuid-char")
    public UUID id;

    private String refreshToken;

    @Type(type = "uuid-char")
    private UUID userId;
}
