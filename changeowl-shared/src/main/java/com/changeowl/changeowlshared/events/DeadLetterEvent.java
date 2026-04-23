package com.changeowl.changeowlshared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeadLetterEvent {
    private Object originalEvent;
    private String errorMessage;
    private String exceptionType;
    private long timestamp;
}
