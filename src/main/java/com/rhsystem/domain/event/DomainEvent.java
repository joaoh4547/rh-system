package com.rhsystem.domain.event;

import java.time.Instant;

/**
 * Marker for domain events: immutable facts about something that already
 * happened in the domain ("user created", "password reset requested").
 *
 * <p>Events are named in the past tense and carry only values (ids, strings) —
 * never JPA entities — so they can safely cross threads and transactions.</p>
 */
public interface DomainEvent {

    /** When the fact happened. */
    Instant occurredOn();
}
