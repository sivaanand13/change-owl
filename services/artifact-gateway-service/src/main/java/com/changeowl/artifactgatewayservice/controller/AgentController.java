package com.changeowl.artifactgatewayservice.controller;

import com.changeowl.artifactgatewayservice.integration.AgentClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentClient agentClient;

    public AgentController(AgentClient agentClient) {
        this.agentClient = agentClient;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamSearch(
            @RequestHeader(value = "X-Session-ID", required = true) String sessionId,
            @RequestBody Map<String, String> body
    ) {

        String question = body.get("question");
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: question");
        }

        return agentClient.streamAgentChat(question, sessionId)
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("error")
                                .data("{\"type\":\"error\",\"message\":\"Search agent is currently unreachable.\"}")
                                .build()
                ));
    }
}
