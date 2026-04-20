package com.changeowl.changeowlshared.kafka;

public class KafkaTopics {
    private KafkaTopics() {}

    public static final String TECHNICAL_ARTIFACTS = "technical-artifacts";
    public static final String GITHUB_PR_DLQ = "github.pr.events.dlq";
}
