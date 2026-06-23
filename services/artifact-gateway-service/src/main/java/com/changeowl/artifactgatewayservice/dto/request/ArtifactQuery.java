package com.changeowl.artifactgatewayservice.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ArtifactQuery {
    private String q;
    private Integer limit = 10;
    private Integer offset = 0;

    private String changeType;
    private String surface;
    private String risk;
    private String confidence;
    private String impact;

    private Integer relatedTo;
    private Integer repoId;
}
