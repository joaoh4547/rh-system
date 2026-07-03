package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class GetUserByUserName {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("error.user.not.found"));
    }

}
