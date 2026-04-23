package com.changeowl.storageservice.repository;

import com.changeowl.storageservice.entity.TechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnologyRepository extends JpaRepository<TechnologyEntity, Long> {
        Optional<TechnologyEntity> findBySlug(String slug);
}
