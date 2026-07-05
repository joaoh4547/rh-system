package com.rhsystem.domain.model.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ActivationTokenTest {

    @Test
    void constructorGeneratesUnusedUuidToken() {
        User user = new User();
        ActivationToken token = new ActivationToken(user,
                LocalDateTime.now().plusHours(1), TokenPurpose.ACTIVATION);

        assertNotNull(token.getToken());
        assertEquals(36, token.getToken().length()); // canonical UUID
        assertSame(user, token.getUser());
        assertFalse(token.isUsed());
        assertEquals(TokenPurpose.ACTIVATION, token.getPurpose());
    }

    @Test
    void tokensAreUniquePerInstance() {
        LocalDateTime exp = LocalDateTime.now().plusHours(1);
        ActivationToken a = new ActivationToken(new User(), exp, TokenPurpose.ACTIVATION);
        ActivationToken b = new ActivationToken(new User(), exp, TokenPurpose.ACTIVATION);
        assertFalse(a.getToken().equals(b.getToken()));
    }

    @Test
    void freshTokenIsValid() {
        ActivationToken token = new ActivationToken(new User(),
                LocalDateTime.now().plusMinutes(5), TokenPurpose.PASSWORD_RESET);
        assertTrue(token.isValid());
    }

    @Test
    void expiredTokenIsInvalid() {
        ActivationToken token = new ActivationToken(new User(),
                LocalDateTime.now().minusSeconds(1), TokenPurpose.ACTIVATION);
        assertFalse(token.isValid());
    }

    @Test
    void usedTokenIsInvalidEvenBeforeExpiry() {
        ActivationToken token = new ActivationToken(new User(),
                LocalDateTime.now().plusHours(1), TokenPurpose.ACTIVATION);
        token.setUsed(true);
        assertFalse(token.isValid());
    }
}
