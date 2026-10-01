package com.rhsystem.application.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rhsystem.application.usecase.usuario.GetUserSummary;
import com.rhsystem.application.usecase.usuario.RemoveUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * Defense in depth: use cases are protected by {@code @PreAuthorize} (method
 * security), independently of the Vaadin views. Same context as
 * {@code RhSystemApplicationTests} (shared/cached by the Spring test framework).
 */
@SpringBootTest
@ActiveProfiles("test")
class UseCaseAuthorizationTest {

    @Autowired
    private GetUserSummary getUserSummary;

    @Autowired
    private RemoveUser removeUser;

    @Test
    void anonymousCallIsRejected() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> getUserSummary.execute());
    }

    @Test
    @WithMockUser(roles = "VIEW_GROUP")
    void userWithoutTheFunctionalityIsDenied() {
        assertThrows(AccessDeniedException.class, () -> getUserSummary.execute());
    }

    @Test
    @WithMockUser(roles = "VIEW_USER")
    void userWithTheFunctionalityIsAllowed() {
        assertDoesNotThrow(() -> getUserSummary.execute());
    }

    @Test
    @WithMockUser(roles = "VIEW_USER")
    void viewingDoesNotGrantDeleting() {
        // denied before touching the database (id does not even exist)
        assertThrows(AccessDeniedException.class, () -> removeUser.execute(-1L));
    }
}
