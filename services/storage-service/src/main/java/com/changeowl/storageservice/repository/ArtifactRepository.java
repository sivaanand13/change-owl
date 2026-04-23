package com.changeowl.storageservice.repository;

import com.changeowl.storageservice.entity.ArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtifactRepository extends JpaRepository<ArtifactEntity, Long> {

    Optional<ArtifactEntity> findBySourceAndTypeAndExternalIdAndRepoId(
            String source,
            String type,
            String externalId,
            Integer repoId
    );

}
