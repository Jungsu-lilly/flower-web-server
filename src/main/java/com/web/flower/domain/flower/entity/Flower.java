package com.web.flower.domain.flower.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@Table(name = "flower")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Flower {

    @Id
    @Type(type = "uuid-char")
    private UUID id;

    private String name;

    private String meaning;

    private String flowerLanguage;

    private String floweringTime;

    private boolean test;

    private String color;

    private boolean poison;

    private String information;
}
