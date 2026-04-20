package com.changeowl.changeowlshared.model;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ArtifactEvent implements Event {

    private String source;
    private String type;
    private String externalId;

    private String owner;
    private String repo;

    private String title;
    private String body;
    private String author;
    private String url;
    private String state;

    private Instant sourceCreatedAt;
    private Instant sourceUpdatedAt;

    private String rawPayload;

    @Override
    public String source() {
        return source;
    }

    @Override
    public String eventType() {
        return type;
    }

    @Override
    public String eventId() {
        return String.valueOf(externalId);
    }

    @Override
    public String context() {
        return owner + "/" + repo;
    }
}