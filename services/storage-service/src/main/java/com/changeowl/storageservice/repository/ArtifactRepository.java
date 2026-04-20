package com.changeowl.storageservice.repository;

import com.changeowl.storageservice.entity.ArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtifactRepository extends JpaRepository<ArtifactEntity, Long> {

    boolean existsBySourceAndTypeAndExternalId(String source, String type, String externalId);
}
