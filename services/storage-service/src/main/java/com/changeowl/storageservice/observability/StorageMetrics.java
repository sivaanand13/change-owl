package com.changeowl.storageservice.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class StorageMetrics {
    private final MeterRegistry meterRegistry;

    public StorageMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementStorageOperation(String step, String status, String source) {
        Counter.builder("storage.artifacts.total")
                .tag("step", step)
                .tag("status", status)
                .tag("source", source)
                .description("Count of artifacts persisted in storage")
                .register(meterRegistry)
                .increment();
    }

    public void incrementKafkaRetry(String type, int attempt) {
        Counter.builder("storage.artifacts.retry")
                .tag("type", type)
                .tag("attempt", String.valueOf(attempt))
                .register(meterRegistry)
                .increment();
    }

    public void incrementStorageEvent(String step, String type, String status) {
        Counter.builder("storage.kafka.publish.total")
                .tag("step", step)
                .tag("type", type)
                .tag("status", status)
                .description("Count of canonical events published to Kafka")
                .register(meterRegistry)
                .increment();
    }

   public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operation) {
        sample.stop(meterRegistry.timer("storage.db.latency", "operation", operation));
   }
}
