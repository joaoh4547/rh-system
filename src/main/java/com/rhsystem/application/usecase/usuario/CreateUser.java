package com.rhsystem.application.usecase.usuario;

import com.rhsystem.domain.model.Functionality.Roles;
import org.springframework.security.access.prepost.PreAuthorize;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.DocumentUpload;
import com.rhsystem.application.port.FileStorage;
import com.rhsystem.application.port.DomainEventPublisher;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.event.UserCreated;
import com.rhsystem.domain.model.parameters.AppParameter;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.Document;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.GroupRepository;
import com.rhsystem.domain.repository.ParameterRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.service.AttachmentPolicy;
import com.rhsystem.domain.service.CpfValidator;
import com.rhsystem.domain.service.UsernameGenerator;
import com.rhsystem.domain.validation.ValidationResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: creates a user (status PENDING), generates username, persists attachments,
 * creates the activation token and publishes {@link UserCreated} (the activation email
 * is sent by a listener, only after this transaction commits).
 */
@PreAuthorize("hasRole('" + Roles.CREATE_USER + "')")
@Service
@AllArgsConstructor
public class CreateUser {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ActivationTokenRepository tokenRepository;
    private final DomainEventPublisher events;
    private final FileStorage fileStorage;
    private final CommandValidator commandValidator;
    private final ParameterRepository parameterRepository;
    private final ParameterValueConverter converter;


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
        validateDocuments(cmd.documents(), validation);
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
                LocalDateTime.now().plusHours(getTokenValidityHours()), TokenPurpose.ACTIVATION);
        tokenRepository.save(token);

        events.publish(UserCreated.of(saved, token.getRawToken()));
        return saved;
    }

    /** Server-side upload rules — the UI limits are only a convenience. */
    private static void validateDocuments(java.util.List<DocumentUpload> documents, ValidationResult validation) {
        if (documents == null) {
            return;
        }
        validation.addIf(documents.size() > AttachmentPolicy.MAX_FILES, "documents", "error.document.too.many");
        for (DocumentUpload upload : documents) {
            validation.addIf(!AttachmentPolicy.isWithinSizeLimit(upload.content()),
                    "documents", "error.document.size");
            validation.addIf(AttachmentPolicy.detectAllowedType(upload.content()).isEmpty(),
                    "documents", "error.document.type");
        }
    }

    private Document createDocument(DocumentUpload upload) {
        String path = fileStorage.store(upload.content(), upload.fileName());
        Document doc = new Document();
        doc.setDescription(upload.description());
        doc.setFileName(upload.fileName());
        // Type detected from the content (already validated), never the client-sent MIME
        doc.setContentType(AttachmentPolicy.detectAllowedType(upload.content()).orElseThrow());
        doc.setStoragePath(path);
        doc.setSize(upload.content() == null ? 0L : (long) upload.content().length);
        return doc;
    }

    private Long getTokenValidityHours() {
        var param = parameterRepository.findByParameter(AppParameter.USER_ACTIVATION_TOKEN_EXPIRATION_TIME_HOURS);
        return param.map(parameter -> Long.parseLong(converter.convert(parameter))).orElse(24L);
    }
}
