package com.rhsystem.application.usecase.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.group.GroupSummary;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Casos de uso de consulta de grupos. */
class GroupQueryUseCasesTest {

    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
    }

    @Nested
    class GetGroupTests {

        @Test
        void loadsGroupWithFunctionalities() {
            Group group = Group.builder().id(1L).name("RH").build();
            when(groupRepository.findByIdWithFunctionalities(1L)).thenReturn(Optional.of(group));

            assertSame(group, new GetGroup(groupRepository).execute(1L));
        }

        @Test
        void missingGroupThrows() {
            when(groupRepository.findByIdWithFunctionalities(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> new GetGroup(groupRepository).execute(1L));
            assertEquals("error.group.not.found", ex.getMessage());
        }
    }

    @Nested
    class ListGroupsTests {

        @Test
        void listsAllGroups() {
            List<Group> groups = List.of(Group.builder().id(1L).build());
            when(groupRepository.findAll()).thenReturn(groups);

            assertEquals(groups, new ListGroups(groupRepository).execute());
        }

        @Test
        void listsOnlyActiveGroupsForSelectionWidgets() {
            List<Group> active = List.of(Group.builder().id(1L).active(true).build());
            when(groupRepository.findAllActive()).thenReturn(active);

            assertEquals(active, new ListGroups(groupRepository).executeActive());
        }

        @Test
        void paginatedListPassesLimitAndOffsetInRepositoryOrder() {
            List<Sorting> sorting = List.of(new Sorting("name", Sorting.Direction.ASC));
            Group group = Group.builder().id(1L).build();
            // Assinatura do repositório é (limit, offset, sorting) — a ordem importa
            when(groupRepository.findAllPaginated(20, 40, sorting)).thenReturn(List.of(group));

            List<Group> page = new ListGroups(groupRepository)
                    .execute(40, 20, sorting).toList();

            assertEquals(List.of(group), page);
            verify(groupRepository).findAllPaginated(20, 40, sorting);
        }
    }

    @Nested
    class GetGroupSummaryTests {

        @Test
        void buildsSummaryFromCounts() {
            when(groupRepository.count()).thenReturn(7L);
            when(groupRepository.countActive()).thenReturn(4L);

            assertEquals(new GroupSummary(7, 4),
                    new GetGroupSummary(groupRepository).execute());
        }
    }
}
