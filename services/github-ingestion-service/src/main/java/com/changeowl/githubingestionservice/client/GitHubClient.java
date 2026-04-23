package com.changeowl.githubingestionservice.client;

import com.changeowl.githubingestionservice.client.dto.GitHubDiscussionDTO;
import com.changeowl.githubingestionservice.client.dto.GitHubPullRequestDTO;
import com.changeowl.githubingestionservice.observability.IngestionMetrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GitHubClient {

    public final static Integer GITHUB_API_RATE_LIMIT = 5000;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final String token;
    private final IngestionMetrics metrics;

    public GitHubClient(@Value("${github.token}") String token, IngestionMetrics metrics) {
        this.token = token;
        this.metrics = metrics;
    }

    public List<GitHubPullRequestDTO> fetchPullRequests(String owner, String repo, Instant lastSyncedAt) {

        var sample = metrics.startIngestionTimer();
        List<GitHubPullRequestDTO> pullRequests = new ArrayList<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2026-03-10");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String gitUrlTemplate = "https://api.github.com/repos/%s/%s/pulls?state=all&sort=updated&direction=desc&per_page=100&page=%d";

            int page = 1;

            while (true) {
                log.info("Fetching pull requests for {}/{} - page {}", owner, repo, page);
                String url = String.format(gitUrlTemplate, owner, repo, page);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                updateRateLimitFromHeaders(response.getHeaders());

                JsonNode root = objectMapper.readTree(response.getBody());

                if (root.isEmpty()) {
                    break;
                }

                boolean stop = false;
                for (JsonNode node : root) {
                    GitHubPullRequestDTO pr = objectMapper.treeToValue(node, GitHubPullRequestDTO.class);

                    if (pr.getUpdatedAt().isBefore(lastSyncedAt) || pr.getUpdatedAt().equals(lastSyncedAt)) {
                        stop = true;
                        break;
                    }

                    pr.setRawJson(node.toString());
                    pullRequests.add(pr);
                }
                log.info("Fetched {} pull requests in page {} for {}/{}", pullRequests.size(), page, owner, repo);

                if (stop) {
                    break;
                }
                page++;
            }
            log.info("Fetched {} pull requests for {}/{}", pullRequests.size(), owner, repo);
            return pullRequests;

        } catch(Exception e) {
            log.error("Critical failure: GitHub API request failed for {}/{}", owner, repo, e);
            return pullRequests;
        } finally {
            metrics.stopIngestionTimer(sample, "fetch_pull_requests");
        }
    }

    private final String graphqlquery = """
        query($owner: String!, $name: String!, $cursor: String) {
          repository(owner: $owner, name: $name) {
            discussions(first: 100, after: $cursor, orderBy: {field: UPDATED_AT, direction: DESC}) {
              nodes {
                id
                title
                bodyText
                url
                createdAt
                updatedAt
                author { login }
                category { name }
                isAnswered
              }
              pageInfo {
                hasNextPage
                endCursor
              }
            }
          }
        }
        """;

    public List<GitHubDiscussionDTO> fetchDiscussions(String owner, String repo, Instant lastSyncedAt) throws JsonProcessingException {
        var sample = metrics.startIngestionTimer();
        List<GitHubDiscussionDTO> discussions = new ArrayList<>();

        String cursor = null;
        boolean stop = false;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        while (!stop) {
            log.info("Fetching discussions for {}/{} - cursor {}", owner, repo, cursor);
            String githubGraphqlUrl = "https://api.github.com/graphql";

            Map<String, Object> variables = cursor == null
                        ? Map.of("owner", owner, "name", repo)
                        : Map.of("owner", owner, "name", repo, "cursor", cursor);
            Map<String, Object> requestBody = Map.of("query", graphqlquery, "variables", variables);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    githubGraphqlUrl,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            updateRateLimitFromHeaders(response.getHeaders());
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode nodes = root
                    .path("data")
                    .path("repository")
                    .path("discussions")
                    .path("nodes");

            JsonNode pageInfo = root
                    .path("data")
                    .path("repository")
                    .path("discussions")
                    .path("pageInfo");

            if (nodes.isEmpty()) {
                break;
            }

            for (JsonNode node : nodes) {
                GitHubDiscussionDTO discussionDTO = objectMapper.treeToValue(node, GitHubDiscussionDTO.class);
                Instant updatedAt = discussionDTO.getUpdatedAt();

                if (updatedAt.isBefore(lastSyncedAt) || updatedAt.equals(lastSyncedAt)) {
                    stop = true;
                    break;
                }

                discussionDTO.setRawJson(node.toString());
                discussions.add(discussionDTO);
            }

            JsonNode endCursorNode = pageInfo.get("endCursor");
            cursor = endCursorNode != null? endCursorNode.asText() : null;

            if (!pageInfo.get("hasNextPage").asBoolean()) {
                break;
            }
            log.info("Fetched {} discussions for {}/{} - cursor {}", discussions.size(), owner, repo, cursor);
        }
        metrics.stopIngestionTimer(sample, "fetch_discussions");
        return discussions;
    }

    private  void updateRateLimitFromHeaders(HttpHeaders headers) {
        String remainingLimit = headers.getFirst("X-RateLimit-Remaining");
        if (remainingLimit != null) {
            metrics.updateRemainingRateLimit(Integer.parseInt(remainingLimit));
        }
    }
}
