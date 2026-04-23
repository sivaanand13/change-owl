package com.changeowl.storageservice.consumer;

import com.changeowl.changeowlshared.kafka.KafkaTopics;
import com.changeowl.changeowlshared.model.ArtifactEvent;
import com.changeowl.storageservice.observability.StorageMetrics;
import com.changeowl.storageservice.service.ArtifactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArtifactConsumer {
    private final ArtifactService artifactService;
    private final String kafkaTopic = KafkaTopics.TECHNICAL_ARTIFACTS;
    private final StorageMetrics storageMetrics;

    @KafkaListener(
            topics = kafkaTopic,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ArtifactEvent event) {
        var timer = storageMetrics.startTimer();
        try {
            log.info("Received artifact event source={}, type={}, externalId={}", event.getSource(), event.getType(), event.getExternalId());
            artifactService.saveArtifact(event);
                storageMetrics.incrementStorageOperation("save_artifact", "success", event.getType());
        } catch (Exception e) {
            log.error("Error processing artifact event source={}, type={}, externalId={}, error={}", event.getSource(), event.getType(), event.getExternalId(), e.getMessage(), e);
            throw e;
        } finally {
            storageMetrics.stopTimer(timer, "db_write");
        }
    }
}
