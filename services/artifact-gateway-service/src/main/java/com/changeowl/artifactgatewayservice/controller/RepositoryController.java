package com.changeowl.artifactgatewayservice.controller;

import com.changeowl.artifactgatewayservice.service.RepositoryService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {
    private final RepositoryService repositoryService;

    @GetMapping
    public ResponseEntity<?> getTrackedRepositories() {
        try {
            return ResponseEntity.ok(repositoryService.getTrackedRepositories());
        } catch (Exception e) {
            log.error("Error fetching tracked repositories", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
