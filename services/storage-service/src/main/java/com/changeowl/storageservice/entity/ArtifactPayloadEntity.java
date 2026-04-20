package com.changeowl.storageservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "artifact_payloads")
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactPayloadEntity {

    @Id
    @Column(name = "artifact_id")
    private Integer artifactId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;
}
