package com.changeowl.artifactgatewayservice.dto.response;
import com.changeowl.artifactgatewayservice.entity.TechnologyEntity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class RepositoryResponse {
    private Integer id;

    private String owner;

    private String name;

    private Boolean isActive;

    private TechnologyEntity technology;

    private LocalDateTime createdAt;

    private Instant lastSyncedAt;
}
