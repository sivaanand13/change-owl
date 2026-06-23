package com.changeowl.artifactgatewayservice.service;

import com.changeowl.artifactgatewayservice.dto.request.ArtifactQuery;
import com.changeowl.artifactgatewayservice.dto.response.ArtifactPageResponse;
import com.changeowl.artifactgatewayservice.dto.response.ArtifactResponse;
import com.changeowl.artifactgatewayservice.integration.SemanticClient;
import com.changeowl.artifactgatewayservice.repository.ArtifactIntelligenceRepository;
import com.changeowl.artifactgatewayservice.repository.ArtifactProjection;
import com.changeowl.artifactgatewayservice.repository.ArtifactRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtifactServiceImpl implements ArtifactService{

    private final ArtifactRepository artifactRepository;
    private final SemanticClient semanticClient;

    private ArtifactResponse toResponse(ArtifactProjection p) {
        try {
            JsonNode intelligence = (new ObjectMapper()).readTree(p.getIntelligence());
            return ArtifactResponse.builder()
                    .id(p.getId())
                    .title(p.getTitle())
                    .body(p.getBody())
                    .url(p.getUrl())
                    .author(p.getAuthor())
                    .state(p.getState())
                    .createdAt(p.getCreatedAt())
                    .repoId(p.getRepoId())
                    .repoName(p.getRepoName())
                    .techId(p.getTechId())
                    .techName(p.getTechName())
                    .intelligence(intelligence)
                    .similarityScore(p.getSimilarityScore())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArtifactPageResponse getArtifacts(ArtifactQuery query) {
        int limit = Math.min(query.getLimit() == null ? 10 : query.getLimit(), 50);
        int offset = query.getOffset() == null ? 0 : query.getOffset();

        String embedding = null;

        List<ArtifactProjection> results;
        if (query.getRelatedTo() != null) {
            embedding = artifactRepository.findEmbeddingByArtifactId(query.getRelatedTo())
                    .orElseThrow(() -> new RuntimeException("Related artifact not found with id: " + query.getRelatedTo()));
            results = artifactRepository.findSimilar(embedding, query.getRelatedTo(), limit, offset);
        } else {

            if (query.getQ() != null && !query.getQ().isBlank()) {
                List<Double> vector = semanticClient.embed(query.getQ());
                embedding = "[" + vector.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")) + "]";
            }

            results = artifactRepository.findArtifacts(
                    query.getChangeType(),
                    query.getSurface(),
                    query.getRisk(),
                    query.getConfidence(),
                    query.getImpact(),
                    embedding,
                    limit,
                    offset
            );
        }

        List<ArtifactResponse> artifacts = results.stream()
                .map(this::toResponse)
                .toList();

        return ArtifactPageResponse.builder()
                .artifacts(artifacts)
                .limit(limit)
                .offset(offset)
                .build();

    }

    @Override
    public ArtifactResponse getArtifactById(Integer id) {
        ArtifactProjection projection = artifactRepository.findArtifactById(id)
                .orElseThrow(() -> new RuntimeException("Artifact not found with id: " + id));

        return toResponse(projection);
    }

    @Override
    public List<ArtifactResponse> getSimilarArtifacts(Integer id, Integer limit) {
        ArtifactPageResponse response =
                getArtifacts(
                        new ArtifactQuery()
                                .setRelatedTo(id)
                                .setLimit(limit)
                );

        return response.getArtifacts();
    }
}
