package com.changeowl.artifactgatewayservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TickerResponse {
    private Long repositories;
    private Long artifacts;
    private Long insights;
}
