package com.changeowl.githubingestionservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequestDTO {

    private Integer number;
    private String title;
    private String body;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String state;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    private User user;

    private String rawJson;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String login;
    }
}