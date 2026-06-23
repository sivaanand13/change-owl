package com.changeowl.artifactgatewayservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ArtifactPageResponse {
    private List<ArtifactResponse> artifacts;
    private Integer limit;
    private Integer offset;
    private Long total;
}
