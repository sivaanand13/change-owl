package com.changeowl.storageservice.repository;

import com.changeowl.storageservice.entity.ArtifactPayloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtifactPayloadRepository extends JpaRepository<ArtifactPayloadEntity, Long> {
}
