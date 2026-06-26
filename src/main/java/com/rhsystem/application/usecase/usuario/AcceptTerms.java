package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: records the user's acceptance of the terms of use. */
@Service
public class AcceptTerms {

    private final UserRepository userRepository;

    public AcceptTerms(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("error.user.not.found"));
        user.acceptTerms();
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
