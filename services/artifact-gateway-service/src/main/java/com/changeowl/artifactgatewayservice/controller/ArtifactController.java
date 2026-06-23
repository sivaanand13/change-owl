package com.changeowl.artifactgatewayservice.controller;

import com.changeowl.artifactgatewayservice.dto.request.ArtifactQuery;
import com.changeowl.artifactgatewayservice.service.ArtifactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    @GetMapping
    public ResponseEntity<?> getArtifacts(@ModelAttribute ArtifactQuery query) {
        return ResponseEntity.ok(artifactService.getArtifacts(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArtifactById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(artifactService.getArtifactById(id));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilarArtifacts(@PathVariable("id") Integer id, @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(artifactService.getSimilarArtifacts(id, limit));
    }
}
