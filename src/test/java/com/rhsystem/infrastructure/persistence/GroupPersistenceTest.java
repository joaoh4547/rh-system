package com.rhsystem.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Testes de persistência do agregado Group contra H2 em memória + Flyway. */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRepositoryAdapter.class, GroupRepositoryAdapter.class,
        ActivationTokenRepositoryAdapter.class})
class GroupPersistenceTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EntityManager em;

    private Group newGroup(String name, boolean active, Functionality... functionalities) {
        return Group.builder()
                .name(name)
                .description("desc " + name)
                .active(active)
                .admin(false)
                .functionalities(new ArrayList<>(List.of(functionalities)))
                .build();
    }

    @Test
    void savesAndReloadsGroupWithFunctionalities() {
        Long id = groupRepository.save(
                newGroup("Gestores", true, Functionality.VIEW_USER, Functionality.CREATE_USER))
                .getId();
        em.flush();
        em.clear();

        Group reloaded = groupRepository.findByIdWithFunctionalities(id).orElseThrow();
        assertEquals("Gestores", reloaded.getName());
        assertEquals(Set.of(Functionality.VIEW_USER, Functionality.CREATE_USER),
                Set.copyOf(reloaded.getFunctionalities()));
    }

    @Test
    void updateReplacesFunctionalitiesCollection() {
        Group group = groupRepository.save(newGroup("Suporte", true, Functionality.VIEW_USER));
        em.flush();
        em.clear();

        Group managed = groupRepository.findByIdWithFunctionalities(group.getId()).orElseThrow();
        managed.getFunctionalities().clear();
        managed.getFunctionalities().add(Functionality.DELETE_USER);
        groupRepository.save(managed);
        em.flush();
        em.clear();

        Group reloaded = groupRepository.findByIdWithFunctionalities(group.getId()).orElseThrow();
        assertEquals(Set.of(Functionality.DELETE_USER), Set.copyOf(reloaded.getFunctionalities()));
    }

    @Test
    void findAllActiveReturnsOnlyActiveGroupsOrderedByName() {
        groupRepository.save(newGroup("Zeta", true));
        groupRepository.save(newGroup("Alpha", true));
        groupRepository.save(newGroup("Beta", false));
        em.flush();

        List<Group> active = groupRepository.findAllActive();
        List<String> names = active.stream().map(Group::getName).toList();

        assertTrue(names.contains("Alpha"));
        assertTrue(names.contains("Zeta"));
        assertFalse(names.contains("Beta"));
        assertTrue(names.indexOf("Alpha") < names.indexOf("Zeta")); // ordenado por nome
        assertTrue(active.stream().allMatch(Group::isActive));
    }

    @Test
    void countAndCountActiveReflectInserts() {
        long totalBefore = groupRepository.count();
        long activeBefore = groupRepository.countActive();

        groupRepository.save(newGroup("Ativo1", true));
        groupRepository.save(newGroup("Inativo1", false));
        em.flush();

        assertEquals(totalBefore + 2, groupRepository.count());
        assertEquals(activeBefore + 1, groupRepository.countActive());
    }

    @Test
    void findAllByIdLoadsOnlyRequestedGroups() {
        Group a = groupRepository.save(newGroup("A", true));
        Group b = groupRepository.save(newGroup("B", true));
        groupRepository.save(newGroup("C", true));
        em.flush();

        List<Group> found = groupRepository.findAllById(List.of(a.getId(), b.getId()));

        assertEquals(Set.of(a.getId(), b.getId()),
                Set.copyOf(found.stream().map(Group::getId).toList()));
    }

    @Test
    void paginationSortsByNameDescending() {
        groupRepository.save(newGroup("AAA", true));
        groupRepository.save(newGroup("ZZZ", true));
        em.flush();

        List<String> names = groupRepository
                .findAllPaginated(100, 0, List.of(new Sorting("name", Sorting.Direction.DESC)))
                .stream().map(Group::getName).toList();

        assertTrue(names.indexOf("ZZZ") < names.indexOf("AAA"));
    }
}
