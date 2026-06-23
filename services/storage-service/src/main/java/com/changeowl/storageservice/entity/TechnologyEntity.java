package com.changeowl.storageservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "technologies")
@Getter
public class  TechnologyEntity {

    @Id
    private Integer id;

    private String name;

    private String slug;
}
