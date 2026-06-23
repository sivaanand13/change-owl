package com.changeowl.artifactgatewayservice.service;

import com.changeowl.artifactgatewayservice.dto.request.ArtifactQuery;
import com.changeowl.artifactgatewayservice.dto.response.ArtifactPageResponse;
import com.changeowl.artifactgatewayservice.dto.response.ArtifactResponse;

import java.util.List;

public interface ArtifactService {

    ArtifactPageResponse getArtifacts(ArtifactQuery query);

    ArtifactResponse getArtifactById(Integer id);

    List<ArtifactResponse> getSimilarArtifacts(Integer id, Integer limit);

}
