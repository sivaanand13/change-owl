package com.changeowl.storageservice.producer;

import com.changeowl.changeowlshared.model.Event;
import com.changeowl.storageservice.observability.StorageMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StorageMetrics storageMetrics;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, StorageMetrics storageMetrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageMetrics = storageMetrics;
    }

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
                            storageMetrics.incrementStorageEvent(step, type, "failure");
                        } else {
                            log.info("Successfully published event type={} id={} context={} topic={}",
                                    event.eventType(),
                                    event.eventId(),
                                    event.context(),
                                    topic
                            );
                            storageMetrics.incrementStorageEvent(step, type, "success");
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
            storageMetrics.incrementStorageEvent(step, type, "failure");
            throw e;
        }
    }
}
