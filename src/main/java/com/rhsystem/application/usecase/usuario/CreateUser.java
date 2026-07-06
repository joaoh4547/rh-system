package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.DocumentUpload;
import com.rhsystem.application.port.FileStorage;
import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.Document;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.GroupRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.service.CpfValidator;
import com.rhsystem.domain.service.UsernameGenerator;
import com.rhsystem.domain.validation.ValidationResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: creates a user (status PENDING), generates username, persists attachments,
 * creates the activation token and sends the email.
 */
@Service
public class CreateUser {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ActivationTokenRepository tokenRepository;
    private final UserNotifier notifier;
    private final FileStorage fileStorage;
    private final CommandValidator commandValidator;
    private final long tokenValidityHours;

    public CreateUser(UserRepository userRepository,
                      GroupRepository groupRepository,
                      ActivationTokenRepository tokenRepository,
                      UserNotifier notifier,
                      FileStorage fileStorage,
                      CommandValidator commandValidator,
                      @Value("${rh-system.ativacao-token-validade-horas:24}") long tokenValidityHours) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.tokenRepository = tokenRepository;
        this.notifier = notifier;
        this.fileStorage = fileStorage;
        this.commandValidator = commandValidator;
        this.tokenValidityHours = tokenValidityHours;
    }

    @Transactional
    public User execute(CreateUserCommand cmd) {
        String cpf = CpfValidator.digitsOnly(cmd.cpf());
        String rg = UserSupport.alphanumericOnly(cmd.rg());

        // Structural rules (annotations) + business rules, all collected at once
        ValidationResult validation = commandValidator.check(cmd);
        validation.addIf(!UserSupport.isBlank(cmd.email())
                        && userRepository.existsByEmail(cmd.email().trim()),
                "email", "error.user.email.duplicate");
        validation.addIf(CpfValidator.isValid(cpf) && userRepository.existsByCpf(cpf),
                "cpf", "error.user.cpf.duplicate");
        validation.addIf(!rg.isBlank() && userRepository.existsByRg(rg),
                "rg", "error.user.rg.duplicate");
        validation.throwIfInvalid();

        User user = new User();
        user.setFirstName(cmd.firstName().trim());
        user.setLastName(cmd.lastName().trim());
        user.setEmail(cmd.email().trim());
        user.setCpf(cpf);
        user.setRg(rg);
        user.setStatus(UserStatus.PENDING_CONFIRMATION);
        user.setUsername(UsernameGenerator.generate(cmd.firstName(), cmd.lastName(),
                userRepository::existsByUsername));
        user.setAddress(UserSupport.toAddress(cmd.address()));
        user.setGroups(new ArrayList<>(groupRepository.findAllById(
                cmd.groupIds() == null ? Set.of() : cmd.groupIds())));

        if (cmd.documents() != null) {
            for (DocumentUpload upload : cmd.documents()) {
                user.addDocument(createDocument(upload));
            }
        }

        User saved = userRepository.save(user);

        ActivationToken token = new ActivationToken(saved,
                LocalDateTime.now().plusHours(tokenValidityHours), TokenPurpose.ACTIVATION);
        tokenRepository.save(token);

        notifier.sendActivation(saved, token.getToken());
        return saved;
    }

    private Document createDocument(DocumentUpload upload) {
        String path = fileStorage.store(upload.content(), upload.fileName());
        Document doc = new Document();
        doc.setDescription(upload.description());
        doc.setFileName(upload.fileName());
        doc.setContentType(upload.contentType());
        doc.setStoragePath(path);
        doc.setSize(upload.content() == null ? 0L : (long) upload.content().length);
        return doc;
    }
}
