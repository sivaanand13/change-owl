package com.changeowl.artifactgatewayservice.repository;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public interface ArtifactProjection {

    Integer getId();
    String getTitle();
    String getBody();
    String getUrl();
    String getAuthor();
    String getState();

    Instant getCreatedAt();

    Integer getRepoId();
    String getRepoName();

    Integer getTechId();
    String getTechName();

    String getIntelligence();

    Double getSimilarityScore();
}