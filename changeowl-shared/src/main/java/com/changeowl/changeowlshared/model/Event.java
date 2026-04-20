package com.changeowl.changeowlshared.model;

public interface Event {
    String source();
    String eventType();
    String eventId();
    String context();
}