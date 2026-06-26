package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.login.LoginResult;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: validates credentials WITHOUT completing authentication (used at login
 * to decide whether to show the terms acceptance before authenticating).
 */
@Service
public class ValidateLogin {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ValidateLogin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResult execute(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .filter(u -> u.getPassword() != null && passwordEncoder.matches(password, u.getPassword()))
                .map(u -> u.termsAccepted() ? LoginResult.OK : LoginResult.TERMS_PENDING)
                .orElse(LoginResult.INVALID_CREDENTIALS);
    }
}
