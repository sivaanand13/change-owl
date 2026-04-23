package com.changeowl.githubingestionservice.mapper;

import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.githubingestionservice.client.dto.GitHubDiscussionDTO;
import com.changeowl.githubingestionservice.client.dto.GitHubPullRequestDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class ArtifactMapper {
    public ArtifactEvent toArtifactEvent(GitHubPullRequestDTO dto, String owner, String repo) {
        return ArtifactEvent.builder()
                .source("github")
                .type("pull_request")
                .externalId(String.valueOf(dto.getNumber()))
                .url(dto.getHtmlUrl())
                .owner(owner)
                .repo(repo)
                .state(dto.getState())
                .title(dto.getTitle())
                .body(dto.getBody())
                .author(dto.getUser().getLogin())
                .rawPayload(dto.getRawJson())
                .sourceUpdatedAt(parseInstant(dto.getUpdatedAt().toString()))
                .sourceCreatedAt(dto.getCreatedAt())
                .build();
    }

    public ArtifactEvent toArtifactEvent(GitHubDiscussionDTO dto, String owner, String repo) {
        StringBuilder enrichedBody = new StringBuilder();
        enrichedBody.append(dto.getBodyText());

        if (dto.isAnswered() && dto.getAnswer() != null) {
            enrichedBody.append("\n\n--- RESOLVED ANSWER BY ")
                    .append(dto.getAnswer().getAuthor().getLogin())
                    .append(" ---\n")
                    .append(dto.getAnswer().getBodyText());
        }

        return ArtifactEvent.builder()
                .source("github")
                .type("discussion")
                .externalId(dto.getId())
                .owner(owner)
                .repo(repo)
                .title(dto.getTitle())
                .body(enrichedBody.toString())
                .author(dto.getAuthor() != null ? dto.getAuthor().getLogin() : "anonymous")

                .state(String.format("STATE: %s | AUTHOR_ROLE: %s | REACTIONS: %d",
                        dto.isAnswered() ? "RESOLVED" : "OPEN",
                        dto.getAuthorAssociation(),
                        dto.getReactions() != null ? dto.getReactions().getTotalCount() : 0))

                .url(dto.getUrl())
                .rawPayload(dto.getRawJson())
                .sourceUpdatedAt(parseInstant(dto.getUpdatedAt().toString()))
                .sourceCreatedAt(parseInstant(dto.getCreatedAt().toString()))
                .build();
    }

    private Instant parseInstant(String dateStr) {
        return dateStr != null ? Instant.parse(dateStr) : Instant.now();
    }
}