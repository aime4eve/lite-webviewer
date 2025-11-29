package com.documentpreview.shared.ddd;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Domain events represent something that has happened in the domain.
 */
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredOn;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public abstract String getEventType();
}