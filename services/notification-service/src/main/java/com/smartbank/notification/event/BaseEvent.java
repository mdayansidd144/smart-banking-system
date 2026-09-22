package com.smartbank.notification.event;
import java.time.LocalDateTime;
import java.util.UUID;
public abstract class BaseEvent {

    private UUID eventId;
    private LocalDateTime occurredAt;

    public BaseEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
    }
    public UUID getEventId() { return eventId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}