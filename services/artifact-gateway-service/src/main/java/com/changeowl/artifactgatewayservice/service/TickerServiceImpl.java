package com.changeowl.artifactgatewayservice.service;

import com.changeowl.artifactgatewayservice.dto.response.TickerResponse;
import com.changeowl.artifactgatewayservice.repository.ArtifactIntelligenceRepository;
import com.changeowl.artifactgatewayservice.repository.ArtifactRepository;
import com.changeowl.artifactgatewayservice.repository.TrackedRepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TickerServiceImpl implements TickerService {
    private final TrackedRepoRepository trackedRepoRepository;
    private final ArtifactRepository artifactRepository;
    private final ArtifactIntelligenceRepository artifactIntelligenceRepository;

    public TickerResponse getTicker() {
        Long repositories = trackedRepoRepository.count();
        Long artifacts = artifactRepository.count();
        Long insights = artifactIntelligenceRepository.count();

        return TickerResponse.builder()
                .repositories(repositories)
                .artifacts(artifacts)
                .insights(insights)
                .build();
    }
}
