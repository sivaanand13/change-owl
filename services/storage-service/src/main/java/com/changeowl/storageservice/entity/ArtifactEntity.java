package com.changeowl.storageservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;

@Entity
@Table(name = "artifacts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ArtifactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tech_id")
    private Integer techId;

    @Column(name = "repo_id")
    private Integer repoId;

    @Builder.Default
    private String source = "github";

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "type")
    private String type;

    private String title;

    private String body;

    private String url;

    private String author;

    private String state;

    @Column(name = "source_created_at")
    private Instant sourceCreatedAt;

    @Column(name = "source_updated_at")
    private Instant sourceUpdatedAt;

    @Column(name = "processed_at", insertable = false, updatable = false)
    private Instant processedAt;
}
