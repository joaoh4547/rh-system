package com.rhsystem.application.usecase.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.group.CreateGroupCommand;
import com.rhsystem.application.dto.group.EnableGroupCommand;
import com.rhsystem.application.dto.group.UpdateGroupCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Casos de uso de escrita de grupos (criar, atualizar, ativar/desativar). */
class GroupCommandUseCasesTest {

    private static ValidatorFactory factory;
    private static CommandValidator commandValidator;

    private GroupRepository groupRepository;

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
        groupRepository = mock(GroupRepository.class);
    }

    @Nested
    class CreateGroupTests {

        @Test
        void buildsGroupFromCommandAndSaves() {
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

            Group saved = new CreateGroup(commandValidator, groupRepository).execute(
                    new CreateGroupCommand("RH", "Recursos Humanos", true, false,
                            List.of(Functionality.VIEW_USER, Functionality.CREATE_USER)));

            assertEquals("RH", saved.getName());
            assertEquals("Recursos Humanos", saved.getDescription());
            assertTrue(saved.isActive());
            assertFalse(saved.isAdmin());
            assertEquals(Set.of(Functionality.VIEW_USER, Functionality.CREATE_USER),
                    Set.copyOf(saved.getFunctionalities()));
        }

        @Test
        void createsAdminGroup() {
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

            Group saved = new CreateGroup(commandValidator, groupRepository).execute(
                    new CreateGroupCommand("Admins", null, true, true, List.of()));

            assertTrue(saved.isAdmin());
        }
    }

    @Nested
    class UpdateGroupTests {

        @Test
        void unknownGroupThrows() {
            when(groupRepository.findByIdWithFunctionalities(9L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> new UpdateGroup(groupRepository).execute(
                            new UpdateGroupCommand(9L, "X", null, true, false, List.of())));
            assertEquals("error.group.not.found", ex.getMessage());
            verify(groupRepository, never()).save(any());
        }

        @Test
        void replacesFieldsAndFunctionalities() {
            Group existing = Group.builder()
                    .id(1L).name("Antigo").description("desc").active(true).admin(false)
                    .functionalities(new ArrayList<>(List.of(Functionality.VIEW_USER)))
                    .build();
            when(groupRepository.findByIdWithFunctionalities(1L)).thenReturn(Optional.of(existing));
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

            Group updated = new UpdateGroup(groupRepository).execute(new UpdateGroupCommand(
                    1L, "Novo", "nova desc", false, true,
                    List.of(Functionality.CREATE_GROUP, Functionality.DELETE_GROUP)));

            assertEquals("Novo", updated.getName());
            assertEquals("nova desc", updated.getDescription());
            assertFalse(updated.isActive());
            assertTrue(updated.isAdmin());
            assertEquals(Set.of(Functionality.CREATE_GROUP, Functionality.DELETE_GROUP),
                    Set.copyOf(updated.getFunctionalities()));
        }

        @Test
        void nullFunctionalitiesClearsCollection() {
            Group existing = Group.builder()
                    .id(1L).name("G").active(true)
                    .functionalities(new ArrayList<>(List.of(Functionality.VIEW_USER)))
                    .build();
            when(groupRepository.findByIdWithFunctionalities(1L)).thenReturn(Optional.of(existing));
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

            Group updated = new UpdateGroup(groupRepository).execute(
                    new UpdateGroupCommand(1L, "G", null, true, false, null));

            assertTrue(updated.getFunctionalities().isEmpty());
        }
    }

    @Nested
    class EnableGroupTests {

        @Test
        void disablesGroup() {
            Group group = Group.builder().id(1L).active(true).build();
            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

            new EnableGroup(groupRepository).execute(new EnableGroupCommand(1L, false));

            assertFalse(group.isActive());
            verify(groupRepository).save(group);
        }

        @Test
        void reenablesGroup() {
            Group group = Group.builder().id(1L).active(false).build();
            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

            new EnableGroup(groupRepository).execute(new EnableGroupCommand(1L, true));

            assertTrue(group.isActive());
        }

        @Test
        void unknownGroupThrows() {
            when(groupRepository.findById(9L)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> new EnableGroup(groupRepository).execute(new EnableGroupCommand(9L, true)));
        }
    }
}
