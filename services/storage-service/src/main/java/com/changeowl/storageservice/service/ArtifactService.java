package com.changeowl.storageservice.service;

import com.changeowl.changeowlshared.kafka.KafkaTopics;
import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.changeowlshared.model.CanonicalArtifactEvent;
import com.changeowl.storageservice.entity.ArtifactEntity;
import com.changeowl.storageservice.entity.ArtifactPayloadEntity;
import com.changeowl.storageservice.entity.TechnologyEntity;
import com.changeowl.storageservice.entity.TrackedRepoEntity;
import com.changeowl.storageservice.mapper.ArtifactMapper;
import com.changeowl.storageservice.producer.KafkaEventPublisher;
import com.changeowl.storageservice.repository.ArtifactPayloadRepository;
import com.changeowl.storageservice.repository.ArtifactRepository;
import com.changeowl.storageservice.repository.TechnologyRepository;
import com.changeowl.storageservice.repository.TrackedRepoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final TrackedRepoRepository trackedRepoRepository;
    private final TechnologyRepository technologyRepository;
    private final ArtifactPayloadRepository artifactPayloadRepository;
    private final ArtifactMapper artifactMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    public ArtifactService(ArtifactRepository artifactRepository, TrackedRepoRepository trackedRepoRepository, TechnologyRepository technologyRepository, ArtifactPayloadRepository artifactPayloadRepository, ArtifactMapper artifactMapper, KafkaEventPublisher kafkaEventPublisher) {
        this.artifactRepository = artifactRepository;
        this.trackedRepoRepository = trackedRepoRepository;
        this.technologyRepository = technologyRepository;
        this.artifactPayloadRepository = artifactPayloadRepository;
        this.artifactMapper = artifactMapper;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Transactional
    public void saveArtifact(ArtifactEvent event) {
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

        Optional<ArtifactEntity> existingArtifact = artifactRepository.findBySourceAndTypeAndExternalIdAndRepoId(event.getSource(), event.getType(), event.getExternalId(), repo.getId());

        ArtifactEntity savedEntity;
        boolean shouldPublish = true;

        if (existingArtifact.isPresent()) {
            savedEntity = existingArtifact.get();

            boolean contentChanged =
                    !Objects.equals(savedEntity.getBody(), event.getBody()) ||
                    !Objects.equals(savedEntity.getTitle(), event.getTitle()) ||
                    !Objects.equals(savedEntity.getAuthor(), event.getAuthor()) ||
                    !Objects.equals(savedEntity.getState(), event.getState()) ||
                    !Objects.equals(savedEntity.getUrl(), event.getUrl());
            if (contentChanged) {
                log.info("Artifact already exists, updating payload: source={}, type={}, externalId={}", event.getSource(), event.getType(), event.getExternalId());
                savedEntity.updateFromEvent(event);
                artifactRepository.save(savedEntity);
            } else {
                log.info("No semantic changes detected for artifact: source={}, type={}, externalId={}", event.getSource(), event.getType(), event.getExternalId());
                shouldPublish = false;
            }

        } else {
            log.info("Creating new artifact: source={}, type={}, externalId={}", event.getSource(), event.getType(), event.getExternalId());
            ArtifactEntity entity = artifactMapper.toEntity(event, repo.getId(), technologyId);
            savedEntity = artifactRepository.save(entity);
        }

        ArtifactPayloadEntity payloadEntity = artifactPayloadRepository.findById(Long.valueOf(savedEntity.getId()))
                .orElse(new ArtifactPayloadEntity());

        payloadEntity.setArtifactId(Math.toIntExact(savedEntity.getId()));
        payloadEntity.setRawPayload(event.getRawPayload());
        artifactPayloadRepository.save(payloadEntity);

        log.info("Saved artifact: source={}, type={}, externalId={}, repo={}/{}", event.getSource(), event.getType(), event.getExternalId(), event.getOwner(), event.getRepo());


        if (shouldPublish) {
            CanonicalArtifactEvent canonicalEvent = CanonicalArtifactEvent.builder()
                    .artifactId(savedEntity.getId())
                    .type(event.getType())
                    .source(event.getSource())
                    .repo(event.context())
                    .build();
            kafkaEventPublisher.publish(KafkaTopics.TECHNICAL_ARTIFACTS_CANONICAL, String.valueOf(savedEntity.getId()), canonicalEvent);
            log.info("Published canonical artifact event: artifactId={}, source={}, type={}, repo={}/{}", savedEntity.getId(), event.getSource(), event.getType(), event.getOwner(), event.getRepo());
        }
    }
}
