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

        assertNotNull(token.getRawToken());
        assertEquals(36, token.getRawToken().length()); // canonical UUID
        assertSame(user, token.getUser());
        assertFalse(token.isUsed());
        assertEquals(TokenPurpose.ACTIVATION, token.getPurpose());
    }

    @Test
    void tokensAreUniquePerInstance() {
        LocalDateTime exp = LocalDateTime.now().plusHours(1);
        ActivationToken a = new ActivationToken(new User(), exp, TokenPurpose.ACTIVATION);
        ActivationToken b = new ActivationToken(new User(), exp, TokenPurpose.ACTIVATION);
        assertFalse(a.getRawToken().equals(b.getRawToken()));
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

    @Test
    void onlyTheSha256HashIsKeptInThePersistentField() {
        ActivationToken token = new ActivationToken(new User(),
                LocalDateTime.now().plusHours(1), TokenPurpose.ACTIVATION);

        assertEquals(64, token.getTokenHash().length());
        assertFalse(token.getTokenHash().contains(token.getRawToken()));
        assertEquals(ActivationToken.hash(token.getRawToken()), token.getTokenHash());
        // known vector: sha256("abc")
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                ActivationToken.hash("abc"));
    }
}
