package com.rhsystem.application.listener;

import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.event.PasswordResetRequested;
import com.rhsystem.domain.event.UserCreated;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Policy "when a user is created / asks for a password reset, email them".
 *
 * <ul>
 *   <li>{@code AFTER_COMMIT}: runs only if the use case's transaction commits — a
 *       rolled-back user/token never produces an email, and the token is already
 *       in the database when the user clicks the link.</li>
 *   <li>{@code fallbackExecution = true}: if an event is ever published outside a
 *       transaction, it is still handled (immediately) instead of silently dropped.</li>
 *   <li>{@code @Async}: SMTP runs on a virtual thread ({@code AsyncConfig}); the use
 *       case — and the Vaadin request behind it — does not wait for Gmail. Failures
 *       are logged by the async exception handler and do not undo the user creation.</li>
 * </ul>
 */
@Component
public class UserNotificationListener {

    private final UserNotifier notifier;

    public UserNotificationListener(UserNotifier notifier) {
        this.notifier = notifier;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(UserCreated event) {
        notifier.sendActivation(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PasswordResetRequested event) {
        notifier.sendPasswordReset(event);
    }
}
