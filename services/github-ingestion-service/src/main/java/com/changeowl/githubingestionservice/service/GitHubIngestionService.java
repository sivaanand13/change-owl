package com.changeowl.githubingestionservice.service;

import com.changeowl.changeowlshared.kafka.KafkaTopics;
import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.githubingestionservice.client.GitHubClient;
import com.changeowl.githubingestionservice.mapper.ArtifactMapper;
import com.changeowl.githubingestionservice.producer.EventPublisher;
import com.changeowl.githubingestionservice.repository.TrackedRepositoryDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class GitHubIngestionService {

    private final GitHubClient gitHubClient;
    private final EventPublisher eventPublisher;
    private final ArtifactMapper artifactMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TrackedRepositoryDao trackedRepositoryDao;

    public GitHubIngestionService(GitHubClient gitHubClient, EventPublisher eventPublisher, ArtifactMapper artifactMapper, JdbcTemplate jdbcTemplate, TrackedRepositoryDao trackedRepositoryDao) {
        this.gitHubClient = gitHubClient;
        this.eventPublisher = eventPublisher;
        this.artifactMapper = artifactMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.trackedRepositoryDao = trackedRepositoryDao;
    }

    @Scheduled(fixedRate = 3600000)
    public void runPeriodicSync() {
        trackedRepositoryDao.findAllTrackedRepos().forEach(repo -> {
            try {
                Instant syncStartTime = Instant.now();
                ingest(repo.owner(), repo.name(), repo.lastSyncedAt());

                trackedRepositoryDao.updateLastSynced(repo.owner(), repo.name(), syncStartTime);

            } catch (Exception e) {
                log.error("Sync failed for {}/{}: {}", repo.owner(), repo.name(), e.getMessage());
            }
        });
    }

    public void ingest(String owner, String name, Instant lastSyncedAt) {

        log.info("Starting ingestion for {}/{} since {}", owner, name, lastSyncedAt);
        String repo = owner + "/" + name;
        try {
            gitHubClient.fetchPullRequests(owner, name, lastSyncedAt).forEach(pr -> {
                ArtifactEvent event = artifactMapper.toArtifactEvent(pr, owner, name);
                eventPublisher.publish(KafkaTopics.TECHNICAL_ARTIFACTS, repo, event);
            });
        } catch (Exception e) {
            log.error("Error syncing PRs for {}: {}", repo, e.getMessage());
        }

        try {
            gitHubClient.fetchDiscussions(owner, name, lastSyncedAt).forEach(disc -> {
                ArtifactEvent event = artifactMapper.toArtifactEvent(disc, owner, name);
                eventPublisher.publish(KafkaTopics.TECHNICAL_ARTIFACTS, repo, event);
            });
        } catch (Exception e) {
            log.error("Error syncing Discussions for {}: {}", repo, e.getMessage());
        }
    }
}