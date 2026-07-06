package com.rhsystem.domain.model.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.grupo.Group;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserTest {

    private static Group group(boolean active, boolean admin, Functionality... functionalities) {
        return Group.builder()
                .name("g")
                .active(active)
                .admin(admin)
                .functionalities(new ArrayList<>(List.of(functionalities)))
                .build();
    }

    @Test
    void fullNameJoinsFirstAndLastName() {
        User user = new User();
        user.setFirstName("João");
        user.setLastName("Silva");
        assertEquals("João Silva", user.getFullName());
    }

    @Test
    void newUserStartsPendingConfirmationWithoutPassword() {
        User user = new User();
        assertEquals(UserStatus.PENDING_CONFIRMATION, user.getStatus());
        assertNull(user.getPassword());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void activateSetsPasswordHashAndActivates() {
        User user = new User();
        user.activate("hash");
        assertEquals("hash", user.getPassword());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void resetPasswordReplacesHashWithoutChangingStatus() {
        User user = new User();
        user.setStatus(UserStatus.BLOCKED);
        user.resetPassword("new-hash");
        assertEquals("new-hash", user.getPassword());
        assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    void acceptTermsRecordsTimestamp() {
        User user = new User();
        assertFalse(user.termsAccepted());
        user.acceptTerms();
        assertTrue(user.termsAccepted());
        assertNotNull(user.getTermsAcceptedAt());
    }

    @Test
    void addDocumentKeepsAggregateConsistent() {
        User user = new User();
        Document doc = new Document();
        user.addDocument(doc);
        assertEquals(1, user.getDocuments().size());
        assertSame(user, doc.getUser());
    }

    // ── Permissions ───────────────────────────────────────────────────────────

    @Test
    void isAdminWhenAnyGroupHasAdminFlag() {
        User user = new User();
        user.setGroups(List.of(group(true, false), group(false, true)));
        assertTrue(user.isAdmin());
    }

    @Test
    void isNotAdminWithoutAdminGroups() {
        User user = new User();
        user.setGroups(List.of(group(true, false, Functionality.VIEW_USER)));
        assertFalse(user.isAdmin());
    }

    @Test
    void adminGetsAllFunctionalities() {
        User user = new User();
        // admin flag counts even on an inactive group (current rule: any admin group)
        user.setGroups(List.of(group(false, true)));
        assertEquals(EnumSet.allOf(Functionality.class), user.getUserFunctionalities());
    }

    @Test
    void nonAdminCombinesDirectAndActiveGroupFunctionalities() {
        User user = new User();
        user.setFunctionalities(List.of(Functionality.VIEW_USER));
        user.setGroups(List.of(
                group(true, false, Functionality.CREATE_GROUP, Functionality.VIEW_GROUP),
                group(false, false, Functionality.DELETE_USER) // inactive: must be ignored
        ));

        Set<Functionality> effective = user.getUserFunctionalities();

        assertEquals(Set.of(Functionality.VIEW_USER, Functionality.CREATE_GROUP,
                Functionality.VIEW_GROUP), effective);
        assertFalse(effective.contains(Functionality.DELETE_USER));
    }

    @Test
    void userWithoutGroupsAndFunctionalitiesHasNoPermissions() {
        assertTrue(new User().getUserFunctionalities().isEmpty());
    }
}
