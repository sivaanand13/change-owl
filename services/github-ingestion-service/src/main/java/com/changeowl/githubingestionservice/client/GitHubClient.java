package com.changeowl.githubingestionservice.client;

import com.changeowl.githubingestionservice.client.dto.GitHubDiscussionDTO;
import com.changeowl.githubingestionservice.client.dto.GitHubPullRequestDTO;
import com.changeowl.githubingestionservice.observability.IngestionMetrics;
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
import java.util.Arrays;
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
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = String.format("https://api.github.com/repos/%s/%s/pulls?state=all&sort=updated&direction=desc&since=%s",
                    owner, repo, lastSyncedAt.toString());


            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                for (JsonNode prNode : root) {
                    GitHubPullRequestDTO pr = objectMapper.treeToValue(prNode, GitHubPullRequestDTO.class);
                    pr.setRawJson(prNode.toString());
                    pullRequests.add(pr);
                }
                return pullRequests;
            } catch (Exception e) {
                log.error("Critical failure: GitHub API returned unparseable root JSON for {}/{}", owner, repo, e);
            }
            return pullRequests;
        } catch(Exception e) {
            log.error("Critical failure: GitHub API request failed for {}/{}", owner, repo, e);
            return pullRequests;
        } finally {
            metrics.stopIngestionTimer(sample, "fetch_pull_requests");
        }
    }

    public List<GitHubDiscussionDTO> fetchDiscussions(String owner, String repo, Instant lastSyncedAt) {
        var sample = metrics.startIngestionTimer();
        List<GitHubDiscussionDTO> discussions = new ArrayList<>();

        try {
            String githubGraphqlUrl = "https://api.github.com/graphql";
            String graphqlquery = """
                    query($owner: String!, $name: String!) {
                      repository(owner: $owner, name: $name) {
                        discussions(first: 20, orderBy: {field: CREATED_AT, direction: DESC}) {
                          nodes {
                            updatedAt
                            id
                            title
                            bodyText
                            url
                            createdAt
                            author { login }
                            authorAssociation
                            category { name }
                            isAnswered
                            answer {
                                bodyText
                                author { login }
                            }
                          }
                        }
                      }
                    }
                    """;
            Map<String, Object> variables = Map.of("owner", owner, "name", repo);
            Map<String, Object> requestBody = Map.of("query", graphqlquery, "variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(githubGraphqlUrl, new HttpEntity<>(requestBody, headers), String.class);

                JsonNode root = objectMapper.readTree(response.getBody());

                if (root.has("errors")) {
                    log.error("GitHub GraphQL API returned errors for {}/{}: {}", owner, repo, root.get("errors").toString());
                    return List.of();
                }

                JsonNode nodes = root.path("data").path("repository").path("discussions").path("nodes");

                for (JsonNode node : nodes) {
                    try {
                        GitHubDiscussionDTO dto = objectMapper.treeToValue(node, GitHubDiscussionDTO.class);
                        dto.setRawJson(node.toString());
                        discussions.add(dto);
                    } catch (Exception e) {
                        log.warn("Skipping discussion node due to parsing error: {}", e.getMessage());
                    }
                }
                log.info("Fetched {} discussions for {}/{}", discussions.size(), owner, repo);
                return discussions;
            } catch (Exception e) {
                log.error("Critical failure: GitHub API returned unparseable JSON for {}/{}", owner, repo, e);
                return discussions;
            }
        } catch (Exception e) {
            log.error("Critical failure: GitHub API request failed for {}/{}", owner, repo, e);
            return discussions;
        } finally {
            metrics.stopIngestionTimer(sample, "fetch_discussions");
        }
    }

    private  void updateRateLimitFromHeaders(HttpHeaders headers) {
        String remainingLimit = headers.getFirst("X-RateLimit-Remaining");
        if (remainingLimit != null) {
            metrics.updateRemainingRateLimit(Integer.parseInt(remainingLimit));
        }
    }
}
