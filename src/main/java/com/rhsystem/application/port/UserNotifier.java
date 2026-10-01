package com.rhsystem.application.port;

import com.rhsystem.domain.event.PasswordResetRequested;
import com.rhsystem.domain.event.UserCreated;

/**
 * Output port for user notifications (e.g. activation email).
 * Implemented in the infrastructure layer; invoked by
 * {@link com.rhsystem.application.listener.UserNotificationListener}, never
 * directly by use cases.
 */
public interface UserNotifier {

    /** Sends the activation email containing the link with the token. */
    void sendActivation(UserCreated event);

    /** Sends the password-reset email containing the link with the token. */
    void sendPasswordReset(PasswordResetRequested event);
}
