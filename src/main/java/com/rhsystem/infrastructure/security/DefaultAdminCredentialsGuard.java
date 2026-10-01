package com.rhsystem.infrastructure.security;

import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.service.PasswordPolicy;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production guard for the development seed user (migration V3: {@code admin.teste}
 * / {@code admin123}, a password published in the repository).
 *
 * <p>Outside the {@code dev}/{@code test} profiles, if that user still has the
 * default password at startup:</p>
 * <ul>
 *   <li>with {@code ADMIN_INITIAL_PASSWORD} set (8..72 chars, not common/personal),
 *       the password is replaced by it;</li>
 *   <li>otherwise the user is {@code BLOCKED} — the published credentials never work.</li>
 * </ul>
 * <p>The migration itself cannot be removed (Flyway checksums of applied migrations).</p>
 */
@Slf4j
@Component
@Profile("!dev & !test")
public class DefaultAdminCredentialsGuard implements ApplicationRunner {

    static final String SEED_USERNAME = "admin.teste";
    private static final String SEED_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialPassword;

    public DefaultAdminCredentialsGuard(UserRepository userRepository,
                                        PasswordEncoder passwordEncoder,
                                        @Value("${rh-system.admin-initial-password:}") String initialPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialPassword = initialPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByUsername(SEED_USERNAME)
                .filter(this::stillHasDefaultPassword)
                .ifPresent(this::secure);
    }

    private boolean stillHasDefaultPassword(User user) {
        return user.getPassword() != null && passwordEncoder.matches(SEED_PASSWORD, user.getPassword());
    }

    private void secure(User user) {
        if (isAcceptable(initialPassword, user)) {
            user.resetPassword(passwordEncoder.encode(initialPassword));
            log.warn("Seed user '{}' had the default password; replaced by ADMIN_INITIAL_PASSWORD. "
                    + "Remove the variable from the environment now.", SEED_USERNAME);
        } else {
            user.setStatus(UserStatus.BLOCKED);
            log.error("Seed user '{}' still had the published default password and was BLOCKED. "
                    + "Set ADMIN_INITIAL_PASSWORD (8-72 chars, not a common password) and restart "
                    + "to unblock it with a new password.", SEED_USERNAME);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private static boolean isAcceptable(String password, User user) {
        return password != null
                && password.length() >= PasswordPolicy.MIN_LENGTH
                && password.length() <= PasswordPolicy.MAX_LENGTH
                && !PasswordPolicy.check(password, user.getUsername(), user.getEmail()).hasErrors();
    }
}
