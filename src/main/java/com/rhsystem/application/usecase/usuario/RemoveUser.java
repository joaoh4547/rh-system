package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: removes a user. */
@Service
public class RemoveUser {

    private final UserRepository userRepository;

    public RemoveUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("error.user.not.found"));
        userRepository.delete(user);
    }
}
