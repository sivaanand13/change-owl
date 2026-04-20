package com.changeowl.githubingestionservice.producer;

import com.changeowl.changeowlshared.model.Event;

public interface EventPublisher {

    public <T extends Event> void publish(String topic, String key, T event);
}