package com.documentpreview.shared.ddd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all aggregate roots in the domain model.
 * Aggregate roots are the entry points to the domain model and are responsible for
 * maintaining invariants and publishing domain events.
 * 
 * @param <ID> The type of the aggregate root's identifier.
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * Registers a domain event to be published.
     * 
     * @param event The domain event to register.
     */
    protected void registerEvent(DomainEvent event) {
        Objects.requireNonNull(event, "Domain event cannot be null");
        domainEvents.add(event);
    }

    /**
     * Clears all registered domain events.
     * This should be called after the events have been published.
     */
    public void clearEvents() {
        domainEvents.clear();
    }

    /**
     * Returns the list of registered domain events.
     * 
     * @return An unmodifiable list of domain events.
     */
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    /**
     * Checks if there are any registered domain events.
     * 
     * @return true if there are events, false otherwise.
     */
    public boolean hasEvents() {
        return !domainEvents.isEmpty();
    }
}