package com.changeowl.artifactgatewayservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingResponse {
    private List<Double> embedding;
}
