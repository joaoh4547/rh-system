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

/** Use case: resets the password from a valid reset token. */
@Service
public class ResetPassword {

    private final ActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommandValidator commandValidator;

    public ResetPassword(ActivationTokenRepository tokenRepository,
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
                .orElseThrow(() -> new BusinessException("error.token.reset.invalid"));
        if (token.getPurpose() != TokenPurpose.PASSWORD_RESET || !token.isValid()) {
            throw new BusinessException("error.token.reset.expired");
        }

        User user = token.getUser();
        user.resetPassword(passwordEncoder.encode(cmd.password()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }
}
