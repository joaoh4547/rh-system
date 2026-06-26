package com.rhsystem.application.port;

import com.rhsystem.domain.model.usuario.User;

/**
 * Output port for user notifications (e.g. activation email).
 * Implemented in the infrastructure layer.
 */
public interface UserNotifier {

    /**
     * Sends the activation email containing the link with the token.
     *
     * @param user  newly created user
     * @param token activation token to embed in the link
     */
    void sendActivation(User user, String token);

    /**
     * Sends the password-reset email containing the link with the token.
     */
    void sendPasswordReset(User user, String token);
}
