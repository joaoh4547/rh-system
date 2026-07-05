package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.DocumentUpload;
import com.rhsystem.application.port.FileStorage;
import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.GroupRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.domain.validation.Violation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateUserTest {

    private static final String VALID_CPF = "529.982.247-25";

    private static ValidatorFactory factory;
    private static CommandValidator commandValidator;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private ActivationTokenRepository tokenRepository;
    private UserNotifier notifier;
    private FileStorage fileStorage;
    private CreateUser useCase;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        commandValidator = new CommandValidator(factory.getValidator());
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        groupRepository = mock(GroupRepository.class);
        tokenRepository = mock(ActivationTokenRepository.class);
        notifier = mock(UserNotifier.class);
        fileStorage = mock(FileStorage.class);
        useCase = new CreateUser(userRepository, groupRepository, tokenRepository,
                notifier, fileStorage, commandValidator, 24);
    }

    private static CreateUserCommand command(Set<Long> groupIds, List<DocumentUpload> documents) {
        return new CreateUserCommand("João", "Silva", "joao@example.com",
                VALID_CPF, "MG-12.345.678",
                new AddressDTO("Rua A", "Centro", "10", null, "30000-000"),
                documents, groupIds);
    }

    @Test
    void createsPendingUserWithGeneratedUsernameNormalizedDocumentsAndActivationEmail() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(groupRepository.findAllById(Set.of(5L)))
                .thenReturn(List.of(Group.builder().id(5L).name("RH").active(true).build()));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(command(Set.of(5L), null));

        assertEquals(UserStatus.PENDING_CONFIRMATION, saved.getStatus());
        assertEquals("joao.silva", saved.getUsername());
        assertEquals("52998224725", saved.getCpf());         // máscara removida
        assertEquals("MG12345678", saved.getRg());           // apenas alfanuméricos
        assertEquals("Rua A", saved.getAddress().getStreet());
        assertEquals(1, saved.getGroups().size());

        ArgumentCaptor<ActivationToken> tokenCaptor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        ActivationToken token = tokenCaptor.getValue();
        assertEquals(TokenPurpose.ACTIVATION, token.getPurpose());
        assertTrue(token.isValid());

        verify(notifier).sendActivation(saved, token.getToken());
    }

    @Test
    void resolvesUsernameCollisionWithSuffix() {
        when(userRepository.existsByUsername("joao.silva")).thenReturn(true);
        when(userRepository.existsByUsername("joao.silva1")).thenReturn(false);
        when(groupRepository.findAllById(Set.of())).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(command(null, null));

        assertEquals("joao.silva1", saved.getUsername());
    }

    @Test
    void storesDocumentsThroughFileStoragePort() {
        byte[] content = "conteudo".getBytes(StandardCharsets.UTF_8);
        when(fileStorage.store(content, "rg.pdf")).thenReturn("storage/rg.pdf");
        when(groupRepository.findAllById(Set.of())).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(command(null,
                List.of(new DocumentUpload("RG", "rg.pdf", "application/pdf", content))));

        assertEquals(1, saved.getDocuments().size());
        assertEquals("storage/rg.pdf", saved.getDocuments().getFirst().getStoragePath());
        assertEquals((long) content.length, saved.getDocuments().getFirst().getSize());
        verify(fileStorage).store(content, "rg.pdf");
    }

    @Test
    void collectsAllDuplicateViolationsAtOnceAndDoesNotSave() {
        when(userRepository.existsByEmail("joao@example.com")).thenReturn(true);
        when(userRepository.existsByCpf("52998224725")).thenReturn(true);
        when(userRepository.existsByRg("MG12345678")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.execute(command(null, null)));

        List<String> keys = ex.getViolations().stream().map(Violation::messageKey).toList();
        assertTrue(keys.contains("error.user.email.duplicate"));
        assertTrue(keys.contains("error.user.cpf.duplicate"));
        assertTrue(keys.contains("error.user.rg.duplicate"));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(notifier, never()).sendActivation(any(), anyString());
    }

    @Test
    void rejectsStructurallyInvalidCommandWithoutTouchingRepositories() {
        CreateUserCommand cmd = new CreateUserCommand(" ", null, "not-an-email",
                "111.111.111-11", "", null, null, null);

        ValidationException ex = assertThrows(ValidationException.class, () -> useCase.execute(cmd));

        List<String> keys = ex.getViolations().stream().map(Violation::messageKey).toList();
        assertTrue(keys.contains("error.user.name.required"));
        assertTrue(keys.contains("error.email.invalid"));
        assertTrue(keys.contains("error.cpf.invalid"));
        verify(userRepository, never()).save(any());
    }
}
