package com.changeowl.githubingestionservice.service;

import com.changeowl.changeowlshared.kafka.KafkaTopics;
import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.githubingestionservice.client.GitHubClient;
import com.changeowl.githubingestionservice.client.dto.GitHubDiscussionDTO;
import com.changeowl.githubingestionservice.client.dto.GitHubPullRequestDTO;
import com.changeowl.githubingestionservice.mapper.ArtifactMapper;
import com.changeowl.githubingestionservice.producer.EventPublisher;
import com.changeowl.githubingestionservice.repository.TrackedRepositoryDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class GitHubIngestionService {

    private final GitHubClient gitHubClient;
    private final EventPublisher eventPublisher;
    private final ArtifactMapper artifactMapper;
    private final TrackedRepositoryDao trackedRepositoryDao;
    @Value("${github.start-since}")
    private Instant defaultStartSince;

    public GitHubIngestionService(GitHubClient gitHubClient, EventPublisher eventPublisher, ArtifactMapper artifactMapper, TrackedRepositoryDao trackedRepositoryDao) {
        this.gitHubClient = gitHubClient;
        this.eventPublisher = eventPublisher;
        this.artifactMapper = artifactMapper;
        this.trackedRepositoryDao = trackedRepositoryDao;
    }

    @Scheduled(fixedRate = 3600000)
    public void runPeriodicSync() {
        trackedRepositoryDao.findAllTrackedRepos().forEach(repo -> {
            try {
                Instant effectiveSince = (repo.lastSyncedAt() != null
                        ? repo.lastSyncedAt()
                        : defaultStartSince)
                        .minusSeconds(5);

                Instant newLastSyncedAt =
                        ingest(repo.owner(), repo.name(), effectiveSince);

                trackedRepositoryDao.updateLastSynced(
                        repo.owner(),
                        repo.name(),
                        newLastSyncedAt
                );

            } catch (Exception e) {
                log.error("Sync failed for {}/{}:", repo.owner(), repo.name(), e);
            }
        });
    }

    public Instant ingest(String owner, String name, Instant lastSyncedAt) throws JsonProcessingException {

        log.info("Starting ingestion for {}/{} since {}", owner, name, lastSyncedAt);
        String repo = owner + "/" + name;
        Instant lastFetched = lastSyncedAt;

        // Fetch and publish pull requests
        List<GitHubPullRequestDTO> prs = gitHubClient.fetchPullRequests(owner, name, lastSyncedAt);
        for (GitHubPullRequestDTO pr: prs)
        {
            if (pr.getUpdatedAt().isAfter(lastFetched)) {
                lastFetched = pr.getUpdatedAt();
            }
            ArtifactEvent event = artifactMapper.toArtifactEvent(pr, owner, name);
            String key = String.format("%s:%d", repo,  pr.getNumber());
            eventPublisher.publish(KafkaTopics.TECHNICAL_ARTIFACTS, key, event);
        }

        // Fetch and publish discussions
        List<GitHubDiscussionDTO> discussions = gitHubClient.fetchDiscussions(owner, name, lastSyncedAt);
        for (GitHubDiscussionDTO disc: discussions)
        {
            if (disc.getUpdatedAt().isAfter(lastFetched)) {
                lastFetched = disc.getUpdatedAt();
            }
            ArtifactEvent event = artifactMapper.toArtifactEvent(disc, owner, name);
            String key = String.format("%s:%s", repo,  disc.getId());
            eventPublisher.publish(KafkaTopics.TECHNICAL_ARTIFACTS, key, event);
        }

        gitHubClient.fetchDiscussions(owner, name, lastSyncedAt).forEach(disc -> {
            ArtifactEvent event = artifactMapper.toArtifactEvent(disc, owner, name);
            eventPublisher.publish(KafkaTopics.TECHNICAL_ARTIFACTS, repo, event);
        });

        return lastFetched;
    }
}