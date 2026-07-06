package com.rhsystem.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.usuario.Address;
import com.rhsystem.domain.model.usuario.Document;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.GroupRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.model.grupo.Group;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testes de persistência do agregado User contra H2 em memória
 * (MODE=PostgreSQL) com o schema criado pelas migrations Flyway reais.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRepositoryAdapter.class, GroupRepositoryAdapter.class,
        ActivationTokenRepositoryAdapter.class})
class UserPersistenceTest {

    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EntityManager em;

    /** CPFs válidos e ainda livres (o seed usa 52998224725). */
    private static final String[] FREE_CPFS = {"11144477735", "12345678909", "98765432100"};

    private User newUser(String firstName, String cpf) {
        int n = SEQ.incrementAndGet();
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName("Teste");
        user.setUsername(firstName.toLowerCase() + ".teste" + n);
        user.setEmail(firstName.toLowerCase() + n + "@example.com");
        user.setCpf(cpf);
        user.setRg("RG" + n + firstName.toUpperCase());
        user.setStatus(UserStatus.PENDING_CONFIRMATION);
        return user;
    }

    // ── Migrations / seed ─────────────────────────────────────────────────────

    @Test
    void flywayMigrationsSeedActiveAdminUser() {
        Optional<User> admin = userRepository.findByUsername("admin.teste");

        assertTrue(admin.isPresent());
        assertEquals(UserStatus.ACTIVE, admin.get().getStatus()); // exige a migration que converte 'ATIVO'
        assertEquals("admin@rhsystem.com", admin.get().getEmail());
        assertEquals("Admin Teste", admin.get().getFullName());
    }

    // ── Round trips ───────────────────────────────────────────────────────────

    @Test
    void savesAndReloadsUserWithEmbeddedAddress() {
        User user = newUser("Carla", FREE_CPFS[0]);
        user.setAddress(new Address("Rua A", "Centro", "10", "apto 3", "30000-000"));
        Long id = userRepository.save(user).getId();
        em.flush();
        em.clear();

        User reloaded = userRepository.findById(id).orElseThrow();
        assertEquals("Carla", reloaded.getFirstName());
        assertEquals("Rua A", reloaded.getAddress().getStreet());
        assertEquals("30000-000", reloaded.getAddress().getPostalCode());
    }

    @Test
    void cascadesDocumentsWithTheAggregate() {
        User user = newUser("Debora", FREE_CPFS[0]);
        Document doc = new Document();
        doc.setDescription("RG");
        doc.setFileName("rg.pdf");
        doc.setContentType("application/pdf");
        doc.setStoragePath("storage/rg.pdf");
        doc.setSize(3L);
        user.addDocument(doc);

        Long id = userRepository.save(user).getId();
        em.flush();
        em.clear();

        User reloaded = userRepository.findById(id).orElseThrow();
        assertEquals(1, reloaded.getDocuments().size());
        assertEquals("storage/rg.pdf", reloaded.getDocuments().getFirst().getStoragePath());
    }

    @Test
    void findByIdWithGroupsFetchesMemberships() {
        Group group = groupRepository.save(Group.builder()
                .name("RH").active(true).admin(false)
                .functionalities(new ArrayList<>())
                .build());
        User user = newUser("Elisa", FREE_CPFS[0]);
        user.setGroups(new ArrayList<>(List.of(group)));
        Long id = userRepository.save(user).getId();
        em.flush();
        em.clear();

        User reloaded = userRepository.findByIdWithGroups(id).orElseThrow();
        assertEquals(1, reloaded.getGroups().size());
        assertEquals("RH", reloaded.getGroups().iterator().next().getName());
    }

    @Test
    void deleteRemovesUser() {
        User user = userRepository.save(newUser("Fabio", FREE_CPFS[0]));
        em.flush();
        Long id = user.getId();

        userRepository.delete(user);
        em.flush();
        em.clear();

        assertTrue(userRepository.findById(id).isEmpty());
    }

    // ── Buscas e verificações de unicidade ────────────────────────────────────

    @Test
    void findByEmailIsCaseInsensitive() {
        User saved = userRepository.save(newUser("Gina", FREE_CPFS[0]));
        em.flush();

        Optional<User> found = userRepository.findByEmail(saved.getEmail().toUpperCase());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void existsChecksMatchSeedUser() {
        assertTrue(userRepository.existsByUsername("admin.teste"));
        assertTrue(userRepository.existsByEmail("ADMIN@RHSYSTEM.COM")); // case-insensitive
        assertTrue(userRepository.existsByCpf("52998224725"));
        assertTrue(userRepository.existsByRg("123456789"));

        assertFalse(userRepository.existsByUsername("ghost"));
        assertFalse(userRepository.existsByEmail("ghost@example.com"));
        assertFalse(userRepository.existsByCpf(FREE_CPFS[1]));
        assertFalse(userRepository.existsByRg("RG-INEXISTENTE"));
    }

    // ── Contagens e paginação ─────────────────────────────────────────────────

    @Test
    void countByStatusReflectsInserts() {
        int pendingBefore = userRepository.countByStatus(UserStatus.PENDING_CONFIRMATION);
        int totalBefore = userRepository.count();

        userRepository.save(newUser("Hugo", FREE_CPFS[0]));
        userRepository.save(newUser("Iris", FREE_CPFS[1]));
        em.flush();

        assertEquals(pendingBefore + 2,
                userRepository.countByStatus(UserStatus.PENDING_CONFIRMATION));
        assertEquals(totalBefore + 2, userRepository.count());
    }

    @Test
    void paginationSortsByRequestedFieldAndDirection() {
        userRepository.save(newUser("Zuleica", FREE_CPFS[0]));
        userRepository.save(newUser("Beatriz", FREE_CPFS[1]));
        userRepository.save(newUser("Marcos", FREE_CPFS[2]));
        em.flush();

        List<String> asc = userRepository
                .findPaginated(0, 100, List.of(new Sorting("firstName", Sorting.Direction.ASC)))
                .stream().map(User::getFirstName).toList();
        assertEquals(asc.stream().sorted().toList(), asc);

        List<String> desc = userRepository
                .findPaginated(0, 100, List.of(new Sorting("firstName", Sorting.Direction.DESC)))
                .stream().map(User::getFirstName).toList();
        assertEquals(desc.stream().sorted(Comparator.reverseOrder()).toList(), desc);
    }

    @Test
    void paginationWithoutSortingFallsBackToFirstNameAscending() {
        userRepository.save(newUser("Zora", FREE_CPFS[0]));
        userRepository.save(newUser("Alan", FREE_CPFS[1]));
        em.flush();

        List<String> names = userRepository.findPaginated(0, 100, null)
                .stream().map(User::getFirstName).toList();
        assertEquals(names.stream().sorted().toList(), names);
    }

    @Test
    void paginationRespectsLimitAndOffset() {
        userRepository.save(newUser("Kaio", FREE_CPFS[0]));
        userRepository.save(newUser("Lia", FREE_CPFS[1]));
        em.flush();

        List<Sorting> byName = List.of(new Sorting("firstName", Sorting.Direction.ASC));
        List<User> firstPage = userRepository.findPaginated(0, 1, byName);
        List<User> secondPage = userRepository.findPaginated(1, 1, byName);

        assertEquals(1, firstPage.size());
        assertEquals(1, secondPage.size());
        assertFalse(firstPage.getFirst().getId().equals(secondPage.getFirst().getId()));
    }
}
