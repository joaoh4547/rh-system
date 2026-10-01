package com.rhsystem.application.port;

import com.rhsystem.domain.event.DomainEvent;

/**
 * Output port used by use cases to announce domain events.
 *
 * <p>Use cases only state <i>what happened</i>; who reacts (email, audit, ...) and
 * <i>when</i> (e.g. only after the transaction commits) is decided by the listeners.
 * Implemented in the infrastructure layer on top of Spring's event bus.</p>
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
