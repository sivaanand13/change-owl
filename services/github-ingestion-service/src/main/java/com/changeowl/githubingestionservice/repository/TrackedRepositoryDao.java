package com.changeowl.githubingestionservice.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class TrackedRepositoryDao {

    private final JdbcTemplate jdbcTemplate;

    public TrackedRepositoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RepoSyncDetails> findAllTrackedRepos() {
        String sql = "SELECT owner, name, last_synced_at FROM tracked_repositories";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RepoSyncDetails(
                rs.getString("owner"),
                rs.getString("name"),
                rs.getTimestamp("last_synced_at") != null ?
                        rs.getTimestamp("last_synced_at").toInstant() : Instant.EPOCH
        ));
    }

    public void updateLastSynced(String owner, String name, Instant lastSyncedAt) {
        String sql = "UPDATE tracked_repositories SET last_synced_at = ? WHERE owner = ? AND name = ?";
        jdbcTemplate.update(sql, java.sql.Timestamp.from(lastSyncedAt), owner, name);
    }

    public record RepoSyncDetails(String owner, String name, Instant lastSyncedAt) {}
}