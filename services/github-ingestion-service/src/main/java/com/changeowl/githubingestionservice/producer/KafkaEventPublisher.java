package com.changeowl.githubingestionservice.producer;

import com.changeowl.changeowlshared.model.Event;
import com.changeowl.githubingestionservice.observability.IngestionMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IngestionMetrics ingestionMetrics;
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, IngestionMetrics ingestionMetrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.ingestionMetrics = ingestionMetrics;
    }

    @Override
    public <T extends Event> void publish(String topic, String key, T event) {

        final String step = "kafka_producer";
        final String type = event.eventType();

        try {
            kafkaTemplate.send(topic, key, event)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    log.error("Failed to publish event type={} id={} context={} topic={}",
                                            event.eventType(),
                                            event.eventId(),
                                            event.context(),
                                            topic,
                                            ex
                                    );
                                    ingestionMetrics.incrementArtifactEvent(step, type, "failure");
                                } else {
                                    log.info("Successfully published event type={} id={} context={} topic={}",
                                            event.eventType(),
                                            event.eventId(),
                                            event.context(),
                                            topic
                                    );
                                    ingestionMetrics.incrementArtifactEvent(step, type, "success");
                                }
                            });

        } catch (Exception e) {
            log.error("Failed publishing event type={} id={} context={} topic={}",
                    event.eventType(),
                    event.eventId(),
                    event.context(),
                    topic,
                    e
            );
            ingestionMetrics.incrementArtifactEvent(step, type, "failure");
            throw e;
        }
    }

}