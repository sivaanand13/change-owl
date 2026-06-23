package com.changeowl.artifactgatewayservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import java.time.Instant;

@Entity
@Table(name = "artifact_intelligence")
@Getter
@Setter
@NoArgsConstructor
public class ArtifactIntelligenceEntity {
    @Id
    @Column(name = "artifact_id")
    private Integer artifactId;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "risk_level")
    private String riskLevel;

    private String confidence;

    @Column(name = "embedding_model")
    private String embeddingModel;

    @Column(name = "summarizer_model")
    private String summarizerModel;

    @Type(JsonType.class)
    @Column(name = "ai_summary", columnDefinition = "jsonb")
    private JsonNode aiSummary;

    @Column(name = "processing_status")
    private String processingStatus;

    @Column(name = "enriched_at")
    private Instant enrichedAt;
}
