package com.rhsystem.application.listener;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.rhsystem.application.port.DomainEventPublisher;
import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.event.PasswordResetRequested;
import com.rhsystem.domain.event.UserCreated;
import com.rhsystem.infrastructure.config.AsyncConfig;
import com.rhsystem.infrastructure.event.SpringDomainEventPublisher;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Verifies the event-driven email flow with a real (minimal) Spring context:
 * AFTER_COMMIT semantics, rollback suppression, fallback without a transaction
 * and execution on virtual threads. No Boot, no database schema — just a
 * transaction manager over an empty in-memory H2.
 */
@SpringJUnitConfig(UserNotificationListenerTest.Config.class)
class UserNotificationListenerTest {

    @Configuration
    @EnableTransactionManagement // registers the @TransactionalEventListener factory
    @Import({AsyncConfig.class, UserNotificationListener.class, SpringDomainEventPublisher.class})
    static class Config {

        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .generateUniqueName(true)
                    .build();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        UserNotifier userNotifier() {
            return mock(UserNotifier.class);
        }
    }

    @Autowired
    private DomainEventPublisher events;

    @Autowired
    private UserNotifier notifier;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        reset(notifier);
        tx = new TransactionTemplate(transactionManager);
    }

    private static UserCreated userCreated() {
        return new UserCreated(1L, "joao.silva", "joao@example.com", "João", "tok-123", Instant.now());
    }

    @Test
    void emailIsSentOnlyAfterCommitAndOnAVirtualThread() {
        AtomicBoolean virtual = new AtomicBoolean();
        doAnswer(inv -> {
            virtual.set(Thread.currentThread().isVirtual());
            return null;
        }).when(notifier).sendActivation(any());

        UserCreated event = userCreated();
        tx.executeWithoutResult(status -> {
            events.publish(event);
            // still inside the transaction: nothing may have been sent yet
            verify(notifier, after(200).never()).sendActivation(any());
        });

        verify(notifier, timeout(2_000)).sendActivation(event);
        assertTrue(virtual.get(), "email must be sent on a virtual thread");
    }

    @Test
    void rollbackNeverSendsTheEmail() {
        tx.executeWithoutResult(status -> {
            events.publish(new PasswordResetRequested(1L, "joao.silva", "joao@example.com",
                    "João", "tok-456", Instant.now()));
            status.setRollbackOnly();
        });

        verify(notifier, after(500).never()).sendPasswordReset(any());
    }

    @Test
    void withoutTransactionTheEventIsStillHandled() {
        UserCreated event = userCreated();

        events.publish(event); // fallbackExecution = true

        verify(notifier, timeout(2_000)).sendActivation(event);
    }

    @Test
    void notifierFailureDoesNotPropagateToThePublisher() {
        doAnswer(inv -> {
            throw new IllegalStateException("smtp down");
        }).when(notifier).sendActivation(any());

        UserCreated event = userCreated();
        tx.executeWithoutResult(status -> events.publish(event)); // must not throw

        verify(notifier, timeout(2_000)).sendActivation(event);
    }
}
