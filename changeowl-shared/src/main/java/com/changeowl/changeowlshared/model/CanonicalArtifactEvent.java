package com.changeowl.changeowlshared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanonicalArtifactEvent implements Event{
    private Integer artifactId;
    private String type;
    private String source;
    private String repo;

    @Override
    public String source() {
        return this.source;
    }

    @Override
    public String eventType() {
        return this.type;
    }

    @Override
    public String eventId() {
        return String.valueOf(this.artifactId);
    }

    @Override
    public String context() {
        return this.repo;
    }
}
