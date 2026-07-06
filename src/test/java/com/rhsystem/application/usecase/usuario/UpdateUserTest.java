package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.application.dto.usuario.UpdateUserCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.GroupRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.domain.validation.Violation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateUserTest {

    private static final String CURRENT_CPF = "52998224725";
    private static final String OTHER_VALID_CPF = "111.444.777-35";

    private static ValidatorFactory factory;
    private static CommandValidator commandValidator;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private UpdateUser useCase;
    private User existing;

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
        useCase = new UpdateUser(userRepository, groupRepository, commandValidator);

        existing = new User();
        existing.setFirstName("João");
        existing.setLastName("Silva");
        existing.setUsername("joao.silva");
        existing.setEmail("joao@example.com");
        existing.setCpf(CURRENT_CPF);
        existing.setRg("MG123");
        existing.setGroups(new ArrayList<>(List.of(Group.builder().id(9L).build())));
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    }

    private static UpdateUserCommand command(String email, String cpf, String rg,
                                             Set<Long> groupIds) {
        return new UpdateUserCommand(1L, "João", "Silva", email, cpf, rg,
                UserStatus.ACTIVE, new AddressDTO("Rua B", "Bairro", "20", "apto 1", "31000-000"),
                groupIds);
    }

    @Test
    void unknownIdThrowsUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateUserCommand cmd = new UpdateUserCommand(99L, "A", "B", "a@b.com",
                CURRENT_CPF, "MG123", UserStatus.ACTIVE, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(cmd));
        assertEquals("error.user.not.found", ex.getMessage());
    }

    @Test
    void updatesFieldsStatusAddressAndGroupsAndSetsUpdatedAt() {
        when(groupRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(Group.builder().id(2L).name("Novo").build()));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = useCase.execute(command("novo@example.com", CURRENT_CPF, "MG123", Set.of(2L)));

        assertEquals("novo@example.com", updated.getEmail());
        assertEquals(UserStatus.ACTIVE, updated.getStatus());
        assertEquals("Rua B", updated.getAddress().getStreet());
        assertEquals(1, updated.getGroups().size());
        assertEquals(2L, updated.getGroups().iterator().next().getId());
        assertNotNull(updated.getUpdatedAt());
        // username é imutável e não faz parte do comando
        assertEquals("joao.silva", updated.getUsername());
    }

    @Test
    void nullGroupIdsClearsAllGroups() {
        when(groupRepository.findAllById(Set.of())).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = useCase.execute(command("joao@example.com", CURRENT_CPF, "MG123", null));

        assertTrue(updated.getGroups().isEmpty());
    }

    @Test
    void unchangedValuesSkipDuplicateChecks() {
        when(groupRepository.findAllById(Set.of())).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // mesmo email (case-insensitive), mesmo CPF e mesmo RG
        useCase.execute(command("JOAO@EXAMPLE.COM", CURRENT_CPF, "MG-123", null));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).existsByCpf(anyString());
        verify(userRepository, never()).existsByRg(anyString());
    }

    @Test
    void changedValuesAlreadyTakenCollectAllViolations() {
        when(userRepository.existsByEmail("outro@example.com")).thenReturn(true);
        when(userRepository.existsByCpf("11144477735")).thenReturn(true);
        when(userRepository.existsByRg("SP999")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.execute(command("outro@example.com", OTHER_VALID_CPF, "SP-999", null)));

        List<String> keys = ex.getViolations().stream().map(Violation::messageKey).toList();
        assertTrue(keys.contains("error.user.email.duplicate"));
        assertTrue(keys.contains("error.user.cpf.duplicate"));
        assertTrue(keys.contains("error.user.rg.duplicate"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void nullIdFailsStructuralValidation() {
        UpdateUserCommand cmd = new UpdateUserCommand(null, "A", "B", "a@b.com",
                CURRENT_CPF, "MG123", UserStatus.ACTIVE, null, null);

        // findById(null) não deve nem ser consultado com sucesso — o comando é inválido
        when(userRepository.findById(null)).thenReturn(Optional.of(existing));

        ValidationException ex = assertThrows(ValidationException.class, () -> useCase.execute(cmd));
        assertTrue(ex.getViolations().stream()
                .map(Violation::messageKey)
                .anyMatch("error.user.not.found"::equals));
    }
}
