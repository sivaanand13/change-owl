package com.changeowl.artifactgatewayservice.repository;

import com.changeowl.artifactgatewayservice.entity.ArtifactPayloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtifactPayloadRepository extends JpaRepository<ArtifactPayloadEntity, Long> {
}
