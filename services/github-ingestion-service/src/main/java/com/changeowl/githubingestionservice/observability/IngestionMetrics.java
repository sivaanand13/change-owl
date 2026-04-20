package com.changeowl.githubingestionservice.observability;


import com.changeowl.githubingestionservice.client.GitHubClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IngestionMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger remainingRateLimit = new AtomicInteger(GitHubClient.GITHUB_API_RATE_LIMIT);

    public IngestionMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        Gauge.builder("github.api.rate_limit.remaining", remainingRateLimit, AtomicInteger::get)
                .description("Remaining GitHub API rate limit")
                .register(meterRegistry);
    }

    public void incrementArtifactEvent(String step, String source, String status) {
        Counter.builder("ingestion.artifacts.total")
                .description("Number of artifact events processed")
                .tag("step", step)
                .tag("source", source)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startIngestionTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopIngestionTimer(Timer.Sample sample, String operation) {
        sample.stop(meterRegistry.timer("ingestion.latency", "operation", operation));
    }

    public void updateRemainingRateLimit(int remaining) {
        remainingRateLimit.set(remaining);
    }
}
