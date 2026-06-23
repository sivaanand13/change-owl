package com.changeowl.artifactgatewayservice.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "technologies")
@Getter
public class  TechnologyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;
}
