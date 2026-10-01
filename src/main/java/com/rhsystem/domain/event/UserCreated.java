package com.rhsystem.domain.event;

import com.rhsystem.domain.model.usuario.User;
import java.time.Instant;

/**
 * A user was created (status {@code PENDING_CONFIRMATION}) and an activation
 * token was issued for them.
 *
 * @param userId          id of the new user
 * @param username        generated username
 * @param email           where the activation link must be sent
 * @param firstName       used to greet the user
 * @param activationToken token embedded in the activation link (secret — never logged)
 * @param occurredOn      when the user was created
 */
public record UserCreated(Long userId,
                          String username,
                          String email,
                          String firstName,
                          String activationToken,
                          Instant occurredOn) implements DomainEvent {

    public static UserCreated of(User user, String activationToken) {
        return new UserCreated(user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), activationToken, Instant.now());
    }

    /** Token omitted on purpose: events end up in logs/debuggers. */
    @Override
    public String toString() {
        return "UserCreated[userId=" + userId + ", username=" + username + ", occurredOn=" + occurredOn + "]";
    }
}
