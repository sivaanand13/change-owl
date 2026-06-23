package com.changeowl.artifactgatewayservice.repository;

import com.changeowl.artifactgatewayservice.entity.ArtifactIntelligenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtifactIntelligenceRepository extends JpaRepository<ArtifactIntelligenceEntity, Long> {
}
