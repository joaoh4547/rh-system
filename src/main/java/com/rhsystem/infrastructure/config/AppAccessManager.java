package com.rhsystem.infrastructure.config;

import com.rhsystem.application.port.AccessManager;
import com.rhsystem.domain.model.Functionality;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Arrays;
import org.springframework.stereotype.Service;

/**
 * {@link AccessManager} backed by the authorities of the authenticated user.
 *
 * <p>Authorities are built at login by {@link AppUserDetailsService} from the
 * user's own functionalities plus those of their enabled groups (admin groups
 * grant everything), as {@code ROLE_<FUNCTIONALITY>}. Reading them from the
 * security context keeps this check consistent with {@code @RolesAllowed} on
 * views and {@code @PreAuthorize} on use cases, and costs no database round-trip. Consequence: permission
 * changes take effect on the user's next login.</p>
 */
@Service
public class AppAccessManager implements AccessManager {

    private final AuthenticationContext authenticationContext;

    public AppAccessManager(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public boolean hasAccess(Functionality functionality) {
        return authenticationContext.hasRole(functionality.name());
    }

    @Override
    public boolean hasAccessAny(Functionality... functionalities) {
        return authenticationContext.hasAnyRole(names(functionalities));
    }

    @Override
    public boolean hasAccessAll(Functionality... functionalities) {
        return authenticationContext.hasAllRoles(names(functionalities));
    }

    private static String[] names(Functionality... functionalities) {
        return Arrays.stream(functionalities).map(Functionality::name).toArray(String[]::new);
    }
}
