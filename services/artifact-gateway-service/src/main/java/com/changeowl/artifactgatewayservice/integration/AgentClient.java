package com.changeowl.artifactgatewayservice.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class AgentClient {

    private final WebClient webClient;

    public AgentClient(
            WebClient.Builder webClientBuilder,
            @Value("${changeowl.agent-service.url}") String agentServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(agentServiceUrl).build();
    }

    public Flux<ServerSentEvent<String>> streamAgentChat(String question, String sessionId) {
        return this.webClient.post()
                .uri("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Session-ID", sessionId)
                .bodyValue(Map.of("question", question))
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> ServerSentEvent.<String>builder()
                        .data(data)
                        .build()
                );
    }
}
