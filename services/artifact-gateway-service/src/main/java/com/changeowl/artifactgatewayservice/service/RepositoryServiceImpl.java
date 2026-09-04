package com.changeowl.artifactgatewayservice.service;

import com.changeowl.artifactgatewayservice.dto.response.RepositoryResponse;
import com.changeowl.artifactgatewayservice.entity.TrackedRepoEntity;
import com.changeowl.artifactgatewayservice.repository.TrackedRepoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final TrackedRepoRepository trackedRepoRepository;

    @Override
    public List<RepositoryResponse> getTrackedRepositories() {
        List<TrackedRepoEntity> trackedRepoEntities = trackedRepoRepository.findAll();
        return trackedRepoEntities.stream().map(this::mapToRepositoryResponse).toList();
    }

    private RepositoryResponse mapToRepositoryResponse(TrackedRepoEntity entity) {
        return RepositoryResponse.builder()
                .id(entity.getId())
                .owner(entity.getOwner())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .technology(entity.getTechnology())
                .createdAt(entity.getCreatedAt())
                .lastSyncedAt(entity.getLastSyncedAt())
                .build();
    }
}
