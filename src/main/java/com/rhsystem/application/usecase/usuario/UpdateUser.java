package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.UpdateUserCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.service.CpfValidator;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: updates an existing user's data. */
@Service
public class UpdateUser {

    private final UserRepository userRepository;

    public UpdateUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User execute(UpdateUserCommand cmd) {
        User user = userRepository.findById(cmd.id())
                .orElseThrow(() -> new BusinessException("error.user.not.found"));

        String cpf = CpfValidator.digitsOnly(cmd.cpf());
        String rg = UserSupport.alphanumericOnly(cmd.rg());
        if (!CpfValidator.isValid(cpf)) {
            throw new BusinessException("error.cpf.invalid");
        }
        if (!user.getEmail().equalsIgnoreCase(cmd.email()) && userRepository.existsByEmail(cmd.email())) {
            throw new BusinessException("error.user.email.duplicate");
        }
        if (!user.getCpf().equals(cpf) && userRepository.existsByCpf(cpf)) {
            throw new BusinessException("error.user.cpf.duplicate");
        }
        if (!user.getRg().equals(rg) && userRepository.existsByRg(rg)) {
            throw new BusinessException("error.user.rg.duplicate");
        }

        user.setFirstName(cmd.firstName().trim());
        user.setLastName(cmd.lastName().trim());
        user.setEmail(cmd.email().trim());
        user.setCpf(cpf);
        user.setRg(rg);
        user.setStatus(cmd.status());
        user.setAddress(UserSupport.toAddress(cmd.address()));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
