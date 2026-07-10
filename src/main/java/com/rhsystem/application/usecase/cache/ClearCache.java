package com.rhsystem.application.usecase.cache;

import com.rhsystem.application.port.CacheManagementPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClearCache {

    private final CacheManagementPort cacheManagement;

    public ClearCache(CacheManagementPort cacheManagement) {
        this.cacheManagement = cacheManagement;
    }

    public void execute(String cacheName) {
        if (cacheName == null) {
            cacheManagement.clearAllCaches();
        } else {
            cacheManagement.clearCache(cacheName);
        }
    }
}
