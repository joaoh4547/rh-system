package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads a user by id with the {@code groups} collection initialized, so the
 * edit form can read the memberships after the session is closed (avoids
 * {@code LazyInitializationException} on detached grid entities).
 */
@AllArgsConstructor
@Service
public class GetUser {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(Long id) {
        return userRepository.findByIdWithGroups(id)
                .orElseThrow(() -> new BusinessException("error.user.not.found"));
    }
}
