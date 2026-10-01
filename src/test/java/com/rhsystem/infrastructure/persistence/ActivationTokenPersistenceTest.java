package com.rhsystem.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Persistência de tokens de ativação/redefinição contra H2 em memória + Flyway. */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRepositoryAdapter.class, GroupRepositoryAdapter.class,
        ActivationTokenRepositoryAdapter.class})
class ActivationTokenPersistenceTest {

    @Autowired
    private ActivationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Test
    void savesAndFindsTokenByValue() {
        User admin = userRepository.findByUsername("admin.teste").orElseThrow();
        ActivationToken token = new ActivationToken(admin,
                LocalDateTime.now().plusHours(24), TokenPurpose.PASSWORD_RESET);

        tokenRepository.save(token);
        em.flush();
        em.clear();

        ActivationToken reloaded = tokenRepository.findByToken(token.getRawToken()).orElseThrow();
        assertEquals(admin.getId(), reloaded.getUser().getId());
        assertEquals(TokenPurpose.PASSWORD_RESET, reloaded.getPurpose());
        assertFalse(reloaded.isUsed());
        assertTrue(reloaded.isValid());
    }

    @Test
    void consumedTokenStaysConsumedAfterReload() {
        User admin = userRepository.findByUsername("admin.teste").orElseThrow();
        ActivationToken token = new ActivationToken(admin,
                LocalDateTime.now().plusHours(1), TokenPurpose.ACTIVATION);
        tokenRepository.save(token);
        em.flush();

        token.setUsed(true);
        tokenRepository.save(token);
        em.flush();
        em.clear();

        ActivationToken reloaded = tokenRepository.findByToken(token.getRawToken()).orElseThrow();
        assertTrue(reloaded.isUsed());
        assertFalse(reloaded.isValid());
    }

    @Test
    void unknownTokenIsEmpty() {
        assertTrue(tokenRepository.findByToken("does-not-exist").isEmpty());
    }

    @Test
    void databaseStoresOnlyTheHash() {
        User admin = userRepository.findByUsername("admin.teste").orElseThrow();
        ActivationToken token = new ActivationToken(admin,
                LocalDateTime.now().plusHours(1), TokenPurpose.ACTIVATION);
        tokenRepository.save(token);
        em.flush();

        Object stored = em.createNativeQuery("SELECT token FROM rh_activation_token WHERE id = :id")
                .setParameter("id", token.getId())
                .getSingleResult();

        assertEquals(ActivationToken.hash(token.getRawToken()), stored);
        assertTrue(tokenRepository.findByToken(String.valueOf(stored)).isEmpty(),
                "the stored hash must not work as a link token");
    }

    @Test
    void invalidateActiveTokensBurnsOnlyUnusedTokensOfThatPurpose() {
        User admin = userRepository.findByUsername("admin.teste").orElseThrow();
        ActivationToken reset1 = tokenRepository.save(new ActivationToken(admin,
                LocalDateTime.now().plusMinutes(30), TokenPurpose.PASSWORD_RESET));
        ActivationToken reset2 = tokenRepository.save(new ActivationToken(admin,
                LocalDateTime.now().plusMinutes(30), TokenPurpose.PASSWORD_RESET));
        ActivationToken activation = tokenRepository.save(new ActivationToken(admin,
                LocalDateTime.now().plusHours(24), TokenPurpose.ACTIVATION));
        em.flush();

        int burned = tokenRepository.invalidateActiveTokens(admin, TokenPurpose.PASSWORD_RESET);
        em.clear();

        assertEquals(2, burned);
        assertFalse(tokenRepository.findByToken(reset1.getRawToken()).orElseThrow().isValid());
        assertFalse(tokenRepository.findByToken(reset2.getRawToken()).orElseThrow().isValid());
        assertTrue(tokenRepository.findByToken(activation.getRawToken()).orElseThrow().isValid());
    }
}
