package com.rhsystem.domain.event;

import com.rhsystem.domain.model.usuario.User;
import java.time.Instant;

/**
 * A password reset was requested for an existing user and a reset token was issued.
 *
 * @param userId     id of the user
 * @param username   username (shown in the email)
 * @param email      where the reset link must be sent
 * @param firstName  used to greet the user
 * @param resetToken token embedded in the reset link (secret — never logged)
 * @param occurredOn when the reset was requested
 */
public record PasswordResetRequested(Long userId,
                                     String username,
                                     String email,
                                     String firstName,
                                     String resetToken,
                                     Instant occurredOn) implements DomainEvent {

    public static PasswordResetRequested of(User user, String resetToken) {
        return new PasswordResetRequested(user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), resetToken, Instant.now());
    }

    /** Token omitted on purpose: events end up in logs/debuggers. */
    @Override
    public String toString() {
        return "PasswordResetRequested[userId=" + userId + ", username=" + username + ", occurredOn=" + occurredOn + "]";
    }
}
