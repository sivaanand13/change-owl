package com.changeowl.storageservice.mapper;

import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.storageservice.entity.ArtifactEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class ArtifactMapper {

    public ArtifactEntity toEntity(ArtifactEvent event, Integer repoId, Integer techId) {
        if (event == null) return null;

        return ArtifactEntity.builder()
                .repoId(repoId)
                .techId(techId)
                .source(event.getSource())
                .externalId(event.getExternalId())
                .type(event.getType())
                .title(event.getTitle())
                .body(event.getBody())
                .url(event.getUrl())
                .author(event.getAuthor())
                .state(event.getState())
                .sourceUpdatedAt(event.getSourceUpdatedAt())
                .sourceCreatedAt(event.getSourceCreatedAt())
                .build();
    }
}