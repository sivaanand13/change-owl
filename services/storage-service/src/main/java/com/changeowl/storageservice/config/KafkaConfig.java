package com.changeowl.storageservice.config;

import com.changeowl.storageservice.observability.StorageMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate, StorageMetrics storageMetrics) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> {
            storageMetrics.incrementStorageOperation("dlq", "failure", record.topic());
            log.error("Moving to DLQ: topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), ex.getMessage(), ex);
            return new TopicPartition(record.topic() + ".DLQ", record.partition());
        });

        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        handler.setRetryListeners((record, ex, attempt) -> {
            log.warn("Retrying message: topic={}, partition={}, offset={}, attempt={}, error={}",
                    record.topic(), record.partition(), record.offset(), attempt, ex.getMessage());
            storageMetrics.incrementKafkaRetry(record.topic(), attempt);
        });

        return handler;
    }
}
