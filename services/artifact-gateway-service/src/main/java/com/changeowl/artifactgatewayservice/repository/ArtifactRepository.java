package com.changeowl.artifactgatewayservice.repository;

import com.changeowl.artifactgatewayservice.entity.ArtifactEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ArtifactRepository extends JpaRepository<ArtifactEntity, Long> {

    @Query(value = """
        SELECT embedding::text
        FROM artifact_intelligence
        WHERE artifact_id = :artifactId
        """, nativeQuery = true)
    Optional<String> findEmbeddingByArtifactId(
            @Param("artifactId") Integer artifactId
    );

    @Query(value = """
        SELECT
            a.id AS id,
            a.title AS title,
            a.body AS body,
            a.url AS url,
            a.author AS author,
            a.state AS state,
            a.source_created_at AS createdAt,

            a.repo_id AS repoId,
            r.name AS repoName,

            a.tech_id AS techId,
            t.name AS techName,

            ai.ai_summary AS intelligence,

            1 - (ai.embedding <=> CAST(:embedding AS vector))
                AS similarityScore,
            COUNT(*) OVER() AS total
        FROM artifacts a
        JOIN artifact_intelligence ai
            ON ai.artifact_id = a.id
        JOIN tracked_repositories r
            ON r.id = a.repo_id
        JOIN technologies t
            ON t.id = a.tech_id
        WHERE ai.embedding IS NOT NULL AND (:excludeId IS NULL OR a.id <> :excludeId)
        ORDER BY ai.embedding <=> CAST(:embedding AS vector)

        LIMIT :limit
        OFFSET :offset
        """, nativeQuery = true)
    List<ArtifactProjection> findSimilar(
            @Param("embedding") String embedding,
            @Param("excludeId") Integer excludeId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );


    @Query(value = """
        SELECT
            a.id AS id,
            a.title AS title,
            a.body AS body,
            a.url AS url,
            a.author AS author,
            a.state AS state,
            a.source_created_at AS createdAt,
    
            a.repo_id AS repoId,
            r.name AS repoName,
    
            a.tech_id AS techId,
            t.name AS techName,
    
            ai.ai_summary AS intelligence,
    
            CASE
                WHEN :embedding IS NOT NULL THEN
                    1 - (ai.embedding <=> CAST(:embedding AS vector))
                ELSE NULL
            END AS similarityScore,
            COUNT(*) OVER() AS total
    
        FROM artifacts a
        INNER JOIN artifact_intelligence ai
            ON ai.artifact_id = a.id
        INNER JOIN tracked_repositories r
            ON r.id = a.repo_id
        INNER JOIN technologies t
            ON t.id = a.tech_id
    
        WHERE (:changeType IS NULL OR ai.ai_summary->>'change_type' = :changeType)
          AND (:surface IS NULL OR ai.ai_summary->>'change_surface' = :surface)
          AND (:risk IS NULL OR ai.ai_summary->>'risk_level' = :risk)
          AND (:confidence IS NULL OR ai.ai_summary->>'confidence' = :confidence)
          AND (:impact IS NULL OR ai.ai_summary->>'behavioral_impact' = :impact)
        ORDER BY
            CASE
                WHEN :embedding IS NOT NULL THEN
                    ai.embedding <=> CAST(:embedding AS vector)
                ELSE NULL
            END ASC NULLS LAST,
            a.source_created_at DESC
        LIMIT :limit
        OFFSET :offset
    """, nativeQuery = true)
    List<ArtifactProjection> findArtifacts(
            @Param("changeType") String changeType,
            @Param("surface") String surface,
            @Param("risk") String risk,
            @Param("confidence") String confidence,
            @Param("impact") String impact,
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT
            a.id AS id,
            a.title AS title,
            a.body AS body,
            a.url AS url,
            a.author AS author,
            a.state AS state,
            a.source_created_at AS createdAt,

            a.repo_id AS repoId,
            r.name AS repoName,

            a.tech_id AS techId,
            t.name AS techName,

            ai.ai_summary AS intelligence,

            NULL AS similarityScore,
            COUNT(*) OVER() AS total

        FROM artifacts a
        JOIN artifact_intelligence ai
            ON ai.artifact_id = a.id
        JOIN tracked_repositories r
            ON r.id = a.repo_id
        JOIN technologies t
            ON t.id = a.tech_id
        WHERE a.id = :id
        """, nativeQuery = true)
    Optional<ArtifactProjection> findArtifactById(
            @Param("id") int id
    );

}
