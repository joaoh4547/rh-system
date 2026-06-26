package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.DocumentUpload;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.port.FileStorage;
import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.Document;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.service.CpfValidator;
import com.rhsystem.domain.service.UsernameGenerator;
import java.time.LocalDateTime;
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
    private final ActivationTokenRepository tokenRepository;
    private final UserNotifier notifier;
    private final FileStorage fileStorage;
    private final long tokenValidityHours;

    public CreateUser(UserRepository userRepository,
                      ActivationTokenRepository tokenRepository,
                      UserNotifier notifier,
                      FileStorage fileStorage,
                      @Value("${rh-system.ativacao-token-validade-horas:24}") long tokenValidityHours) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.notifier = notifier;
        this.fileStorage = fileStorage;
        this.tokenValidityHours = tokenValidityHours;
    }

    @Transactional
    public User execute(CreateUserCommand cmd) {
        String cpf = CpfValidator.digitsOnly(cmd.cpf());
        String rg = UserSupport.alphanumericOnly(cmd.rg());

        UserSupport.validateRequired(cmd, cpf, rg);
        if (!CpfValidator.isValid(cpf)) {
            throw new BusinessException("error.cpf.invalid");
        }
        if (userRepository.existsByEmail(cmd.email())) {
            throw new BusinessException("error.user.email.duplicate");
        }
        if (userRepository.existsByCpf(cpf)) {
            throw new BusinessException("error.user.cpf.duplicate");
        }
        if (userRepository.existsByRg(rg)) {
            throw new BusinessException("error.user.rg.duplicate");
        }

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
