package com.changeowl.artifactgatewayservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artifact_id")
    private ArtifactEntity artifact;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;
}
