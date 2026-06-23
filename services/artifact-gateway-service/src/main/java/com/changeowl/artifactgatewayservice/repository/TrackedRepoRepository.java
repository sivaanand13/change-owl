package com.changeowl.artifactgatewayservice.repository;


import com.changeowl.artifactgatewayservice.entity.TrackedRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackedRepoRepository extends JpaRepository<TrackedRepoEntity, Integer> {
    Optional<TrackedRepoEntity> findByOwnerAndName(String owner, String name);
}
