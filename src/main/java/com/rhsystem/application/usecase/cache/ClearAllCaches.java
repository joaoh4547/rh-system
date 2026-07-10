package com.rhsystem.application.usecase.cache;

import com.rhsystem.application.port.CacheAdministration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Evicts all entries of every managed cache region (global flush).
 */
@Service
@AllArgsConstructor
public class ClearAllCaches {

    private final CacheAdministration cacheAdministration;

    public void execute() {
        cacheAdministration.clearAll();
    }
}
