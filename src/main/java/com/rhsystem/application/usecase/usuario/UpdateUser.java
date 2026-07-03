package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.UpdateUserCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.service.CpfValidator;
import com.rhsystem.domain.validation.ValidationResult;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: updates an existing user's data. */
@Service
public class UpdateUser {

    private final UserRepository userRepository;
    private final CommandValidator commandValidator;

    public UpdateUser(UserRepository userRepository, CommandValidator commandValidator) {
        this.userRepository = userRepository;
        this.commandValidator = commandValidator;
    }

    @Transactional
    public User execute(UpdateUserCommand cmd) {
        User user = userRepository.findById(cmd.id())
                .orElseThrow(() -> new BusinessException("error.user.not.found"));

        String cpf = CpfValidator.digitsOnly(cmd.cpf());
        String rg = UserSupport.alphanumericOnly(cmd.rg());

        // Structural rules (annotations) + business rules, all collected at once
        ValidationResult validation = commandValidator.check(cmd);
        validation.addIf(!UserSupport.isBlank(cmd.email())
                        && !user.getEmail().equalsIgnoreCase(cmd.email().trim())
                        && userRepository.existsByEmail(cmd.email().trim()),
                "email", "error.user.email.duplicate");
        validation.addIf(CpfValidator.isValid(cpf)
                        && !user.getCpf().equals(cpf)
                        && userRepository.existsByCpf(cpf),
                "cpf", "error.user.cpf.duplicate");
        validation.addIf(!rg.isBlank()
                        && !user.getRg().equals(rg)
                        && userRepository.existsByRg(rg),
                "rg", "error.user.rg.duplicate");
        validation.throwIfInvalid();

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
