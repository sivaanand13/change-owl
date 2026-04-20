package com.changeowl.githubingestionservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubDiscussionDTO {
    private String id;
    private String title;
    private String bodyText;
    private String url;
    private String createdAt;
    private String updatedAt;
    private Author author;
    private String authorAssociation;

    private Category category;
    private boolean isAnswered;
    private Answer answer;

    private ReactionConnection reactions;

    private String rawJson;

    @Data public static class Author { private String login; }
    @Data public static class Category { private String name; }
    @Data public static class Answer {
        private String bodyText;
        private Author author;
    }
    @Data public static class ReactionConnection {
        private int totalCount;
    }
}