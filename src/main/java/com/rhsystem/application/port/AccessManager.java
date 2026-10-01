package com.rhsystem.application.port;

import com.rhsystem.domain.model.Functionality;

/**
 * Answers "may the current user do X?" — used by the UI to show/hide actions and
 * menu entries. Enforcement itself is done by {@code @RolesAllowed} on views and {@code @PreAuthorize} on
 * use cases; this port only drives what is displayed.
 */
public interface AccessManager {

    boolean hasAccess(Functionality functionality);

    /** {@code true} if the user has at least one of the functionalities. */
    boolean hasAccessAny(Functionality... functionalities);

    /** {@code true} if the user has every one of the functionalities. */
    boolean hasAccessAll(Functionality... functionalities);
}
