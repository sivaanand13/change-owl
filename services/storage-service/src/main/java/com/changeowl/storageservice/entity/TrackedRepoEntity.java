package com.changeowl.storageservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tracked_repositories")
@Getter
@Setter
@NoArgsConstructor
public class TrackedRepoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String owner;
    private String name;

    @Column(name = "tech_id")
    private Integer techId;
}
