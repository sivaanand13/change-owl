package com.changeowl.artifactgatewayservice.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ArtifactResponse {
    private Integer id;
    private String title;
    private String body;
    private String url;
    private String author;
    private String state;

    private Instant createdAt;

    private Integer repoId;
    private String repoName;

    private Integer techId;
    private String techName;

    private JsonNode intelligence;

    private Double similarityScore;
}
