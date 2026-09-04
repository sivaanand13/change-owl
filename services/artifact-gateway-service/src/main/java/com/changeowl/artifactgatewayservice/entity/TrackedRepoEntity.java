package com.changeowl.artifactgatewayservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tracked_repositories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"owner", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TrackedRepoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_id")
    private TechnologyEntity technology;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;
}
