package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.usuario.UserSummary;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Casos de uso de consulta/remoção de usuários — delegação e tratamento de ausência. */
class UserQueryUseCasesTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
    }

    @Nested
    class ListUsersTests {

        @Test
        void listsAllUsers() {
            List<User> users = List.of(new User(), new User());
            when(userRepository.findAll()).thenReturn(users);

            assertEquals(users, new ListUsers(userRepository).execute());
        }

        @Test
        void paginatedListDelegatesOffsetLimitAndSorting() {
            List<Sorting> sorting = List.of(new Sorting("email", Sorting.Direction.DESC));
            User user = new User();
            when(userRepository.findPaginated(40, 20, sorting)).thenReturn(List.of(user));

            List<User> page = new ListUsers(userRepository).execute(40, 20, sorting).toList();

            assertEquals(List.of(user), page);
        }
    }

    @Nested
    class GetUserSummaryTests {

        @Test
        void buildsSummaryFromStatusCounts() {
            when(userRepository.count()).thenReturn(10);
            when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(5);
            when(userRepository.countByStatus(UserStatus.PENDING_CONFIRMATION)).thenReturn(3);
            when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(2);

            UserSummary summary = new GetUserSummary(userRepository).execute();

            assertEquals(new UserSummary(10, 5, 3, 2), summary);
        }
    }

    @Nested
    class GetUserTests {

        @Test
        void loadsUserWithGroupsInitialized() {
            User user = new User();
            when(userRepository.findByIdWithGroups(1L)).thenReturn(Optional.of(user));

            assertSame(user, new GetUser(userRepository).execute(1L));
        }

        @Test
        void missingUserThrows() {
            when(userRepository.findByIdWithGroups(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> new GetUser(userRepository).execute(1L));
            assertEquals("error.user.not.found", ex.getMessage());
        }
    }

    @Nested
    class GetUserByUserNameTests {

        @Test
        void findsByUsername() {
            User user = new User();
            when(userRepository.findByUsername("joao.silva")).thenReturn(Optional.of(user));

            assertSame(user, new GetUserByUserName(userRepository).execute("joao.silva"));
        }

        @Test
        void missingUsernameThrows() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> new GetUserByUserName(userRepository).execute("ghost"));
        }
    }

    @Nested
    class RemoveUserTests {

        @Test
        void removesExistingUser() {
            User user = new User();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            new RemoveUser(userRepository).execute(1L);

            verify(userRepository).delete(user);
        }

        @Test
        void missingUserThrowsAndNothingIsDeleted() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> new RemoveUser(userRepository).execute(1L));
            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    class AcceptTermsTests {

        @Test
        void recordsAcceptanceAndPersists() {
            User user = new User();
            user.setUsername("joao.silva");
            when(userRepository.findByUsername("joao.silva")).thenReturn(Optional.of(user));

            new AcceptTerms(userRepository).execute("joao.silva");

            assertNotNull(user.getTermsAcceptedAt());
            assertNotNull(user.getUpdatedAt());
            verify(userRepository).save(user);
        }

        @Test
        void missingUserThrows() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> new AcceptTerms(userRepository).execute("ghost"));
            verify(userRepository, never()).save(any());
        }
    }
}
