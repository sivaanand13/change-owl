package com.changeowl.artifactgatewayservice.service;
import com.changeowl.artifactgatewayservice.dto.response.RepositoryResponse;

import java.util.List;

public interface RepositoryService {
    public List<RepositoryResponse> getTrackedRepositories() ;
}
