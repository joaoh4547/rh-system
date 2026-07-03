package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.ActivationCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: activates the account by setting the password from the activation token. */
@Service
public class ActivateUser {

    private final ActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommandValidator commandValidator;

    public ActivateUser(ActivationTokenRepository tokenRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        CommandValidator commandValidator) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.commandValidator = commandValidator;
    }

    @Transactional
    public void execute(ActivationCommand cmd) {
        commandValidator.validate(cmd);
        ActivationToken token = tokenRepository.findByToken(cmd.token())
                .orElseThrow(() -> new BusinessException("error.token.activation.invalid"));
        if (token.getPurpose() != TokenPurpose.ACTIVATION || !token.isValid()) {
            throw new BusinessException("error.token.activation.expired");
        }

        User user = token.getUser();
        user.activate(passwordEncoder.encode(cmd.password()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }
}
