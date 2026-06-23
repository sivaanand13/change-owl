package com.changeowl.artifactgatewayservice.integration;

import com.changeowl.artifactgatewayservice.dto.response.EmbeddingResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SemanticClient {
    private final RestClient restClient;

    @Value("${semantic.service.url}")
    private String semanticUrl;

    @Value("${semantic.service.api-key}")
    private String apiKey;

    public List<Double> embed(String query) {
        EmbeddingRequest req = new EmbeddingRequest(query);

        EmbeddingResponse res = restClient.post()
                .uri(semanticUrl + "/embed")
                .header("X-API-Key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(req)
                .retrieve()
                .body(EmbeddingResponse.class);

        return res.getEmbedding();
    }

    public record EmbeddingRequest(
            @JsonProperty("query") String query
    ) {}
}
