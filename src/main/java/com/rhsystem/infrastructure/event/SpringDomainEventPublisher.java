package com.rhsystem.infrastructure.event;

import com.rhsystem.application.port.DomainEventPublisher;
import com.rhsystem.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * {@link DomainEventPublisher} adapter backed by Spring's {@link ApplicationEventPublisher}.
 *
 * <p>Publishing is synchronous and joins the caller's transaction context, which is
 * what lets {@code @TransactionalEventListener}s defer their work until the commit.</p>
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
