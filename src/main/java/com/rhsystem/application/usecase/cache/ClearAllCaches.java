package com.rhsystem.application.usecase.cache;

import com.rhsystem.domain.model.Functionality.Roles;
import org.springframework.security.access.prepost.PreAuthorize;
import com.rhsystem.application.port.CacheAdministration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Evicts all entries of every managed cache region (global flush).
 */
@PreAuthorize("hasRole('" + Roles.MANAGE_CACHE + "')")
@Service
@AllArgsConstructor
public class ClearAllCaches {

    private final CacheAdministration cacheAdministration;

    public void execute() {
        cacheAdministration.clearAll();
    }
}
