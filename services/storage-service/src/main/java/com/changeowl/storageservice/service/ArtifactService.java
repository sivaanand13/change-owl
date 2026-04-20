package com.changeowl.storageservice.service;

import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.storageservice.entity.ArtifactEntity;
import com.changeowl.storageservice.entity.ArtifactPayloadEntity;
import com.changeowl.storageservice.entity.TechnologyEntity;
import com.changeowl.storageservice.entity.TrackedRepoEntity;
import com.changeowl.storageservice.mapper.ArtifactMapper;
import com.changeowl.storageservice.repository.ArtifactPayloadRepository;
import com.changeowl.storageservice.repository.ArtifactRepository;
import com.changeowl.storageservice.repository.TechnologyRepository;
import com.changeowl.storageservice.repository.TrackedRepoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final TrackedRepoRepository trackedRepoRepository;
    private final TechnologyRepository technologyRepository;
    private final ArtifactPayloadRepository artifactPayloadRepository;
    private final ArtifactMapper artifactMapper;

    public ArtifactService(ArtifactRepository artifactRepository, TrackedRepoRepository trackedRepoRepository, TechnologyRepository technologyRepository, ArtifactPayloadRepository artifactPayloadRepository, ArtifactMapper artifactMapper) {
        this.artifactRepository = artifactRepository;
        this.trackedRepoRepository = trackedRepoRepository;
        this.technologyRepository = technologyRepository;
        this.artifactPayloadRepository = artifactPayloadRepository;
        this.artifactMapper = artifactMapper;

    }

    @Transactional
    public void saveArtifact(ArtifactEvent event) {
        if (artifactRepository.existsBySourceAndTypeAndExternalId(event.getSource(), event.getType(), event.getExternalId())) {
            log.info("Artifact already exists: source={}, type={}, externalId={}", event.getSource(), event.getType(), event.getExternalId());
            return;
        }

        TrackedRepoEntity repo = trackedRepoRepository.findByOwnerAndName(event.getOwner(), event.getRepo())
                .orElseThrow(() -> new RuntimeException("Tracked repository not found: " + event.context()));

        Integer technologyId = repo.getTechId();
        if (technologyId == null) {
            log.warn("Technology not found for repository: {}", event.context());
            technologyId = technologyRepository.findBySlug(event.getRepo().toLowerCase())
                    .map(TechnologyEntity::getId)
                    .orElse(null);

            if (technologyId != null) {
                repo.setTechId(technologyId);
                trackedRepoRepository.save(repo);
                log.info("Updated technology for repository {}: techId={}", event.context(), technologyId);
            }
        }

        ArtifactEntity entity = artifactMapper.toEntity(event, repo.getId(), technologyId);
        ArtifactEntity savedEntity = artifactRepository.save(entity);

        ArtifactPayloadEntity payloadEntity = ArtifactPayloadEntity
                .builder()
                .artifactId(savedEntity.getId())
                .rawPayload(event.getRawPayload())
                .build();
        artifactPayloadRepository.save(payloadEntity);

        log.info("Saved artifact: source={}, type={}, externalId={}, repo={}/{}", event.getSource(), event.getType(), event.getExternalId(), event.getOwner(), event.getRepo());

    }
}
